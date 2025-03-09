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
import org.tarlaboratories.tartech.ElectricalNetwork;
import org.tarlaboratories.tartech.ModComponents;
import org.tarlaboratories.tartech.blocks.ComputerBlock;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ComputerBlockEntity extends BlockEntity implements ElectricalNetworkInteractor {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private static final File DRIVES_ROOT = new File("drives");
    private static final ThreadGroup threads = new ThreadGroup("TartechComputerThreadGroup");
    private @Nullable Thread thread;
    private @Nullable File root;
    private @NotNull ItemStack drive;
    private Supplier<ElectricalNetwork> getElectricalNetwork;
    private Consumer<ElectricalNetworkInteractor> onChangedPowerDraw;

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
            LOGGER.info("Interrupting computer thread");
            this.thread.interrupt();
            this.thread = null;
            this.onChangedPowerDraw();
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, @NotNull ComputerBlockEntity entity) {
        if (entity.getEnergySatisfaction() < 1) {
            if (entity.getEnergySatisfaction() > 0) LOGGER.info("Not enough energy, shutting down: {} < 1", entity.getEnergySatisfaction());
            world.setBlockState(pos, state.with(ComputerBlock.IS_ON, false));
            entity.turnOff();
            return;
        }
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
            entity.onChangedPowerDraw();
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
                int result = compiler.run(System.in, System.out, System.err, entrypoint.getPath());
                if (result != 0) {
                    LOGGER.error("Compilation error, exiting");
                    return;
                }
                startJVM("root.Main", drive_dir);
            } catch (ClassFormatError e) {
                LOGGER.warn("Class format error, most probably caused by a previous thread not finishing compilation: {}", e.getMessage());
            }
        });
        entity.onChangedPowerDraw();
        entity.thread.start();
    }

    private @Nullable ElectricalNetwork getElectricalNetwork() {
        if (this.getElectricalNetwork != null) return this.getElectricalNetwork.get();
        else return null;
    }

    private double getEnergySatisfaction() {
        ElectricalNetwork tmp = this.getElectricalNetwork();
        if (tmp == null) return 0;
        else return tmp.getSatisfaction();
    }

    private void onChangedPowerDraw() {
        if (this.onChangedPowerDraw != null) this.onChangedPowerDraw.accept(this);
    }

    private static void startJVM(String cls, File root) {
        String max_memory = "1G";
        String separator = FileSystems.getDefault().getSeparator();
        String classpath = System.getProperty("java.class.path").concat(":.");
        String path = System.getProperty("java.home")
                + separator + "bin" + separator + "java";
        ProcessBuilder processBuilder =
                new ProcessBuilder(path, "-cp",
                        classpath,
                        "-Xmx".concat(max_memory),
                        "-Djava.security.manager",
                        "-Djava.security.policy=java.policy",
                        cls);
        try {
            processBuilder.directory(root);
            File output = new File(root, "out");
            File error = new File(root, "err");
            File policy_file = new File(root, "java.policy");
            boolean success = policy_file.createNewFile();
            if (!success) LOGGER.debug("Didn't create a new policy file, because it already exists");
            FileWriter policy = new FileWriter(policy_file);
            policy.write("grant {permission java.io.FilePermission \"${user.dir}\", \"read,write\";};");
            policy.close();
            processBuilder.redirectOutput(output).redirectError(error);
            LOGGER.info("Starting JVM: {}", processBuilder.command());
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException e) {
            LOGGER.error("Failed to start JVM: {}", e.getMessage());
        } catch (InterruptedException ignored) {}
    }

    public ItemStack getDrive() {
        return this.drive.copy();
    }

    public void setDrive(@NotNull ItemStack newDrive) {
        this.drive = newDrive.copy();
        markDirty();
    }

    @Override
    public double getPowerDraw() {
        return this.thread == null ? 0 : 700;
    }

    @Override
    public void setModifiedCallback(Consumer<ElectricalNetworkInteractor> callback) {
        this.onChangedPowerDraw = callback;
    }

    @Override
    public void setElectricalNetworkGetter(Supplier<ElectricalNetwork> getter) {
        LOGGER.info("setElectricalNetworkGetter has been called");
        this.getElectricalNetwork = getter;
    }
}
