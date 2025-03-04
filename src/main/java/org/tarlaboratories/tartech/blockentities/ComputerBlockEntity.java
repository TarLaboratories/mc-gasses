package org.tarlaboratories.tartech.blockentities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tarlaboratories.tartech.ModBlockEntities;
import org.tarlaboratories.tartech.ModComponents;
import org.tarlaboratories.tartech.blocks.ComputerBlock;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.UUID;

public class ComputerBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private static final File DRIVES_ROOT = new File("drives");
    private static final ThreadGroup threads = new ThreadGroup("TartechComputerThreadGroup");
    private @Nullable Thread thread;
    private @Nullable File root;
    private @NotNull ItemStack drive;

    static {
        if (!DRIVES_ROOT.exists()) {
            boolean success = DRIVES_ROOT.mkdir();
            if (!success) LOGGER.warn("Could not create directory for drives: {}", DRIVES_ROOT.getAbsolutePath());
        }
    }

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER, pos, state);
        drive = ItemStack.EMPTY;
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        drive = ItemStack.fromNbtOrEmpty(registries, (NbtCompound) nbt.get("drive"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        if (!drive.isEmpty()) nbt.put("drive", drive.toNbt(registries));
        else nbt.remove("drive");
    }

    protected void turnOff() {
        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();
            this.thread = null;
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, ComputerBlockEntity entity) {
        if (!state.get(ComputerBlock.IS_ON)) {
            entity.turnOff();
            return;
        }
        ItemStack drive = entity.drive;
        if (drive.isEmpty()) {
            entity.turnOff();
            return;
        }
        UUID root_uuid = drive.get(ModComponents.DATA_STORAGE);
        if (root_uuid == null) {
            drive.set(ModComponents.DATA_STORAGE, UUID.randomUUID());
            root_uuid = drive.get(ModComponents.DATA_STORAGE);
            assert root_uuid != null;
        }
        File root = entity.root;
        if (root == null || !root.getAbsolutePath().contains(root_uuid.toString())) {
            root = new File(DRIVES_ROOT, root_uuid.toString().concat("/root/"));
            if (!root.exists()) {
                boolean success = root.mkdirs();
                if (!success) {
                    LOGGER.warn("Failed to create new directory: {}", root.getAbsolutePath());
                    if (!root.exists()) return;
                }
            }
            boolean success = root.setWritable(true, false);
            if (!success) LOGGER.warn("Failed to change writing permissions for file: {}", root.getAbsolutePath());
            entity.root = root;
            entity.turnOff();
        }
        if (entity.thread != null) {
            if (entity.thread.isAlive()) return;
            LOGGER.info("Computer thread stopped.");
            world.setBlockState(pos, state.with(ComputerBlock.IS_ON, false));
            entity.thread = null;
            return;
        }
        File entrypoint = new File(root, "Main.java");
        if (!entrypoint.exists()) {
            try {
                boolean success = entrypoint.createNewFile();
                if (!success) {
                    LOGGER.warn("Failed to create entrypoint file at {}", entrypoint.getAbsolutePath());
                    if (!entrypoint.exists()) return;
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to create entrypoint file at {}: {}", entrypoint.getAbsolutePath(), e.getMessage());
                if (!entrypoint.exists()) return;
            }
        }
        File drive_dir = root.getParentFile();
        entity.thread = new Thread(threads, () -> {
            try {
                int result = compiler.run(System.in, System.out, System.err, entrypoint.getAbsolutePath());
                if (result != 0) {
                    LOGGER.error("Compilation error, exiting");
                    return;
                }
                RestrictedClassLoader classLoader = new RestrictedClassLoader(new URL[]{drive_dir.toURI().toURL()});
                Class<?> cls = Class.forName("root.Main", true, classLoader);
                Object main = cls.getDeclaredConstructor().newInstance();
                cls.getDeclaredMethod("main").invoke(main);
            } catch (MalformedURLException e) {
                LOGGER.error("This should not happen: {}", e.getMessage());
            } catch (ClassNotFoundException e) {
                LOGGER.error("Couldn't find Main class in computer at {}: {}", pos, e.getMessage());
            } catch (NoSuchMethodException e) {
                LOGGER.error("Cannot find main method in Main class from computer at {}: {}", pos, e.getMessage());
            } catch (InvocationTargetException e) {
                LOGGER.error("Error when invoking main method: {}", e.getMessage());
            } catch (IllegalAccessException e) {
                LOGGER.error("Error when accessing main method: {}", e.getMessage());
            } catch (InstantiationException e) {
                LOGGER.error("Error when instantiating Main class: {}", e.getMessage());
            } catch (ClassFormatError e) {
                LOGGER.warn("Class format error, most probably caused by a previous thread not finishing compilation: {}", e.getMessage());
            }
        });
        entity.thread.start();
    }

    public ItemStack getDrive() {
        return this.drive.copy();
    }

    public void setDrive(@NotNull ItemStack newDrive) {
        this.drive = newDrive.copy();
        markDirty();
    }

    private static class RestrictedClassLoader extends URLClassLoader {
        private static final Set<String> ALLOWED_PACKAGES = Set.of(
                "java.lang.Object",
                "org.tarlaboratories.tartech.computer.",
                "java.lang.Throwable"
        );

        public RestrictedClassLoader(URL[] urls) {
            super(urls);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return validateClass(super.loadClass(name));
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            return validateClass(super.findClass(name));
        }

        public boolean nativeAllowed(String name) {
            return ALLOWED_PACKAGES.stream().anyMatch(name::startsWith);
        }

        public boolean allowed(Class<?> cls) {
            for (Class<?> i : cls.getClasses()) {
                LOGGER.info("idk: {}", i.getName());
            }
            if (nativeAllowed(cls.getName())) return true;
            for (Method i : cls.getDeclaredMethods()) {
                if (Modifier.isNative(i.getModifiers())) {
                    LOGGER.warn("Not allowed native method: {}", i.getName());
                    return false;

                }
            }
            return true;
        }

        public Class<?> validateClass(Class<?> cls) {
            LOGGER.info("Validating class: {}", cls.getName());
            try {
                ClassLoader.class.getDeclaredField("classes");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            if (allowed(cls)) return cls;
            else throw new SecurityException(String.format("Class %s is not allowed", cls.getName()));
        }
    }
}
