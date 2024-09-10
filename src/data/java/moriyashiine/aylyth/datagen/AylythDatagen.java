package moriyashiine.aylyth.datagen;

import moriyashiine.aylyth.datagen.client.AylythEnglishLanguageProvider;
import moriyashiine.aylyth.datagen.client.AylythModelProvider;
import moriyashiine.aylyth.datagen.common.AylythAdvancementProvider;
import moriyashiine.aylyth.datagen.common.AylythDynamicDataProvider;
import moriyashiine.aylyth.datagen.common.AylythRecipeProvider;
import moriyashiine.aylyth.datagen.common.AylythDamageTypeBootstrap;
import moriyashiine.aylyth.datagen.common.levelgen.AylythBiomeBootstrap;
import moriyashiine.aylyth.datagen.common.levelgen.feature.AylythConfiguredCarverBootstrap;
import moriyashiine.aylyth.datagen.common.levelgen.feature.AylythConfiguredFeatureBootstrap;
import moriyashiine.aylyth.datagen.common.levelgen.feature.AylythPlacedFeatureBootstrap;
import moriyashiine.aylyth.datagen.common.levelgen.terrain.AylythDensityFunctionBootstrap;
import moriyashiine.aylyth.datagen.common.levelgen.terrain.AylythNoiseSettingBootstrap;
import moriyashiine.aylyth.datagen.common.levelgen.terrain.AylythNoiseTypeBootstrap;
import moriyashiine.aylyth.datagen.common.loot.AylythBlockLootProvider;
import moriyashiine.aylyth.datagen.common.loot.AylythEntityLootProvider;
import moriyashiine.aylyth.datagen.common.tag.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;

public class AylythDatagen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
        var pack = dataGenerator.createPack();

        var blockTags = pack.addProvider(AylythBlockTagProvider::new);
        pack.addProvider((output, registries) -> new AylythItemTagProvider(output, registries, blockTags));
        pack.addProvider(AylythEntityTypeTagProvider::new);
        pack.addProvider(AylythStatusEffectTagProvider::new);
        pack.addProvider(AylythPotionTagProvider::new);
        pack.addProvider(AylythBiomeTagProvider::new);
        pack.addProvider(AylythDamageTypeTagProvider::new);

        pack.addProvider(AylythModelProvider::new);
        pack.addProvider(AylythEnglishLanguageProvider::new);

        pack.addProvider(AylythRecipeProvider::new);
        pack.addProvider(AylythAdvancementProvider::new);
        pack.addProvider(AylythBlockLootProvider::new);
        pack.addProvider(AylythEntityLootProvider::new);
        pack.addProvider(AylythDynamicDataProvider::new);
    }

    @Override
    public void buildRegistry(RegistryBuilder registryBuilder) {
        registryBuilder.addRegistry(RegistryKeys.NOISE_PARAMETERS, AylythNoiseTypeBootstrap::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.DENSITY_FUNCTION, AylythDensityFunctionBootstrap::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.CHUNK_GENERATOR_SETTINGS, AylythNoiseSettingBootstrap::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_CARVER, AylythConfiguredCarverBootstrap::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, AylythConfiguredFeatureBootstrap::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, AylythPlacedFeatureBootstrap::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.BIOME, AylythBiomeBootstrap::bootstrap);
        registryBuilder.addRegistry(RegistryKeys.DAMAGE_TYPE, AylythDamageTypeBootstrap::bootstrap);
    }
}
