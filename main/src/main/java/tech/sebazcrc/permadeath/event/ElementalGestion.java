package tech.sebazcrc.permadeath.event;

import tech.sebazcrc.permadeath.Main;
import tech.sebazcrc.permadeath.event.player.AbilityListener;
import tech.sebazcrc.permadeath.event.player.CraftingListener;
import tech.sebazcrc.permadeath.util.mob.elementals.ElementalGhast;
import tech.sebazcrc.permadeath.util.mob.elementals.ElementalSpider;

public class ElementalGestion {

    private CraftingListener craftingListener;
    private ElementalSpider elementalSpider;
    private ElementalGhast elementalGhast;
    private AbilityListener abilityListener;

    public ElementalGestion(Main instance) {
        this.craftingListener = new CraftingListener(instance);
        this.elementalGhast = new ElementalGhast(instance);
        this.elementalSpider = new ElementalSpider(instance);
        this.abilityListener = new AbilityListener(instance);

        instance.getServer().getPluginManager().registerEvents(this.craftingListener, instance);
        instance.getServer().getPluginManager().registerEvents(this.elementalGhast, instance);
        instance.getServer().getPluginManager().registerEvents(this.elementalSpider, instance);
        instance.getServer().getPluginManager().registerEvents(this.abilityListener, instance);
    }

    public void onDis() {
        if (this.craftingListener != null) {
            this.craftingListener.saveLimitCraft();
        }
        if (this.elementalSpider != null) {
            this.elementalSpider.saveConfigElemental();
        }
        if (this.elementalGhast != null) {
            this.elementalGhast.saveConfigElemental();
        }
    }

    public CraftingListener getCraftingListener() {
        return craftingListener;
    }

    public ElementalSpider getElementalSpider() {
        return elementalSpider;
    }

    public ElementalGhast getElementalGhast() {
        return elementalGhast;
    }

    public AbilityListener getAbilityListener() {
        return abilityListener;
    }

}
