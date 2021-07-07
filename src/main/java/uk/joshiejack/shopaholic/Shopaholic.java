package uk.joshiejack.shopaholic;

import net.minecraft.data.DataGenerator;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.joshiejack.penguinlib.inventory.AbstractBookContainer;
import uk.joshiejack.penguinlib.item.base.BookItem;
import uk.joshiejack.shopaholic.api.ShopaholicAPI;
import uk.joshiejack.shopaholic.bank.Bank;
import uk.joshiejack.shopaholic.bank.BankAPIImpl;
import uk.joshiejack.shopaholic.client.ShopaholicClientConfig;
import uk.joshiejack.shopaholic.data.ShopaholicBlockStates;
import uk.joshiejack.shopaholic.data.ShopaholicDatabase;
import uk.joshiejack.shopaholic.data.ShopaholicItemModels;
import uk.joshiejack.shopaholic.data.ShopaholicLanguage;
import uk.joshiejack.shopaholic.inventory.DepartmentContainer;
import uk.joshiejack.shopaholic.inventory.EconomyManagerContainer;
import uk.joshiejack.shopaholic.loot.CapValue;
import uk.joshiejack.shopaholic.loot.RatioValue;
import uk.joshiejack.shopaholic.loot.SetValue;
import uk.joshiejack.shopaholic.shipping.Market;
import uk.joshiejack.shopaholic.shop.RegistryImpl;
import uk.joshiejack.shopaholic.shop.comparator.*;
import uk.joshiejack.shopaholic.shop.condition.*;
import uk.joshiejack.shopaholic.shop.listing.*;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(Shopaholic.MODID)
public class Shopaholic {
    public static final String MODID = "shopaholic";
    public static final Logger LOGGER = LogManager.getLogger();

    public Shopaholic() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::setupCommon);
        ShopaholicContainers.CONTAINERS.register(eventBus);
        ShopaholicItems.ITEMS.register(eventBus);
        ShopaholicSounds.SOUNDS.register(eventBus);
        ShopaholicAPI.registry = new RegistryImpl();
        ShopaholicAPI.bank = new BankAPIImpl();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ShopaholicClientConfig.create());
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ShopaholicServerConfig.create());
    }

    private void setupCommon(FMLCommonSetupEvent event) {
        //Register Comparators
        ShopaholicAPI.registry.registerComparator("add", new AddComparator());
        ShopaholicAPI.registry.registerComparator("item_in_hand", new ItemInHandComparator());
        ShopaholicAPI.registry.registerComparator("item_in_inventory", new ItemInInventoryComparator());
        ShopaholicAPI.registry.registerComparator("light_level", new LightLevelComparator());
        ShopaholicAPI.registry.registerComparator("number", new NumberComparator());
        ShopaholicAPI.registry.registerComparator("redstone_level", new RedstoneSignalComparator());
        ShopaholicAPI.registry.registerComparator("shipped", new ShippedCountComparator());
        ShopaholicAPI.registry.registerComparator("block_tag_on_target", new BlockTagOnTargetComparator());
        //Register Conditions
        ShopaholicAPI.registry.registerCondition("and", new AndCondition());
        ShopaholicAPI.registry.registerCondition("block_state", new BlockStateCondition());
        ShopaholicAPI.registry.registerCondition("compare", new CompareCondition());
        ShopaholicAPI.registry.registerCondition("entity_nearby", new EntityNearbyCondition());
        ShopaholicAPI.registry.registerCondition("has_nbt_tag", new HasNBTTagCondition());
        ShopaholicAPI.registry.registerCondition("has_pet", new HasPetCondition());
        ShopaholicAPI.registry.registerCondition("opening_hours", new OpeningHoursCondition());
        ShopaholicAPI.registry.registerCondition("in_dimension", new InDimensionCondition());
        ShopaholicAPI.registry.registerCondition("named", new NamedCondition());
        ShopaholicAPI.registry.registerCondition("or", new OrCondition());
        ShopaholicAPI.registry.registerCondition("per_player", new PerPlayerCondition());
        ShopaholicAPI.registry.registerCondition("shipped", new ShippedCondition());
        ShopaholicAPI.registry.registerCondition("time", new TimeCondition());
        //Register Listing Handlers
        ShopaholicAPI.registry.registerListingHandler("bundle", new BundleListingHandler());
        ShopaholicAPI.registry.registerListingHandler("department", new DepartmentListingHandler());
        ShopaholicAPI.registry.registerListingHandler("entity", new EntityListingHandler());
        ShopaholicAPI.registry.registerListingHandler("heal", new HealListingHandler());
        ShopaholicAPI.registry.registerListingHandler("item", new ItemListingHandler());
        ShopaholicAPI.registry.registerListingHandler("potion", new PotionEffectListingHandler());
        //Register Listing Builders
        //TODO? ListingBuilder.register("food", new FoodBuilder());
        //Cost Formulae
        ShopaholicAPI.registry.registerCostFormula("default", (m, player, listing, level, mechanic, rand) -> listing.getGold());
    }

    @SubscribeEvent
    public static void onDataGathering(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            generator.addProvider(new ShopaholicDatabase(generator));
            generator.addProvider(new ShopaholicBlockStates(generator, event.getExistingFileHelper()));
        }

        if (event.includeClient()) {
            generator.addProvider(new ShopaholicLanguage(generator));
            generator.addProvider(new ShopaholicItemModels(generator, event.getExistingFileHelper()));
        }
    }

    @SubscribeEvent
    public static void registerLootData(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event) {
        Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(MODID, "cap_value"), CapValue.TYPE);
        Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(MODID, "ratio_value"), RatioValue.TYPE);
        Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(MODID, "set_value"), SetValue.TYPE);
    }

    public static class ShopaholicContainers {
        public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, Shopaholic.MODID);
        public static final RegistryObject<ContainerType<AbstractBookContainer>> BOOK = CONTAINERS.register("economy_manager", () -> IForgeContainerType.create((id, inv, data) -> new EconomyManagerContainer(id)));
        public static final RegistryObject<ContainerType<DepartmentContainer>> SHOP = CONTAINERS.register("shop", () -> IForgeContainerType.create((id, inv, data) -> new DepartmentContainer(id, inv.player, data)));
    }

    public static class ShopaholicItems {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Shopaholic.MODID);
        public static final RegistryObject<Item> ECONOMY_MANAGER = ITEMS.register("economy_manager", () -> new BookItem(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC), () -> (id, inv, p) -> new EconomyManagerContainer(id), false));
    }

    public static class ShopaholicSounds {
        public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
        public static final RegistryObject<SoundEvent> KERCHING = createSoundEvent("kerching");

        private static RegistryObject<SoundEvent> createSoundEvent(@Nonnull String name) {
            return SOUNDS.register(name, () -> new SoundEvent(new ResourceLocation(MODID, name)));
        }
    }

    @Mod.EventBusSubscriber(modid = Shopaholic.MODID)
    public static class Sync {
        @SubscribeEvent
        public static void onPlayerJoinedWorld(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getPlayer().level instanceof ServerWorld) {
                Bank.get((ServerWorld) event.getPlayer().level).syncToPlayer((ServerPlayerEntity) event.getPlayer());
                Market.get((ServerWorld) event.getPlayer().level).getShippingForPlayer(event.getPlayer()).syncToPlayer((ServerPlayerEntity) event.getPlayer());
            }
        }
    }
}

