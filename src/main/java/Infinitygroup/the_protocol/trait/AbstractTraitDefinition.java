package Infinitygroup.the_protocol.trait;

import Infinitygroup.the_protocol.The_protocol;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public abstract class AbstractTraitDefinition implements TraitDefinition {
    private final ResourceLocation id;
    private final Set<String> optionalCompatMods;

    protected AbstractTraitDefinition(String path, Set<String> optionalCompatMods) {
        this.id = ResourceLocation.fromNamespaceAndPath(The_protocol.MODID, path);
        this.optionalCompatMods = Set.copyOf(optionalCompatMods);
    }

    @Override
    public final ResourceLocation id() {
        return id;
    }

    @Override
    public final Set<String> optionalCompatMods() {
        return optionalCompatMods;
    }

    @Override
    public Component displayName() {
        return Component.translatable("trait." + The_protocol.MODID + "." + id.getPath() + ".name");
    }

    @Override
    public Component description() {
        return Component.translatable("trait." + The_protocol.MODID + "." + id.getPath() + ".description");
    }
}
