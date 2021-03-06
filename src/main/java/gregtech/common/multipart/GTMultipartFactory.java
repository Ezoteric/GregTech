package gregtech.common.multipart;

import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.api.IPartConverter;
import codechicken.multipart.api.IPartFactory;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.cable.BlockCable;
import gregtech.common.cable.tile.TileEntityCable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class GTMultipartFactory implements IPartFactory, IPartConverter {

    public static final ResourceLocation CABLE_PART_KEY = new ResourceLocation(GTValues.MODID, "cable");

    public static final GTMultipartFactory INSTANCE = new GTMultipartFactory();
    private final Map<ResourceLocation, Supplier<TMultiPart>> partRegistry = new HashMap<>();
    private final Map<Predicate<Block>, BiFunction<IBlockState, TileEntity, TMultiPart>> converterRegistry = new HashMap<>();

    public void registerFactory() {
        registerPart(CABLE_PART_KEY, CableMultiPart::new);
        registerConverter(block -> block instanceof BlockCable, CableMultiPart::new);

        MultiPartRegistry.registerParts(this, partRegistry.keySet());
        MultiPartRegistry.registerConverter(this);
    }

    private void registerPart(ResourceLocation identifier, Supplier<TMultiPart> supplier) {
        partRegistry.put(identifier, supplier);
    }

    private void registerConverter(Predicate<Block> block, BiFunction<IBlockState, TileEntity, TMultiPart> converter) {
        converterRegistry.put(block, converter);
    }

    @Override
    public TMultiPart createPart(ResourceLocation identifier, boolean client) {
        if(partRegistry.containsKey(identifier)) {
            Supplier<TMultiPart> supplier = partRegistry.get(identifier);
            return supplier.get();
        }
        return null;
    }

    private BiFunction<IBlockState, TileEntity, TMultiPart> findConverter(Block block) {
        return converterRegistry.entrySet().stream()
            .filter(entry -> entry.getKey().test(block))
            .map(Entry::getValue)
            .findFirst().orElse(null);
    }

    @Override
    public boolean canConvert(World world, BlockPos pos, IBlockState state) {
        return findConverter(state.getBlock()) != null;
    }

    public TMultiPart convert(World world, BlockPos pos, IBlockState state) {
        BiFunction<IBlockState, TileEntity, TMultiPart> converter = findConverter(state.getBlock());
        if(converter != null) {
            TileEntity tileEntity = world.getTileEntity(pos);
            return converter.apply(state, tileEntity);
        }
        return null;
    }


}
