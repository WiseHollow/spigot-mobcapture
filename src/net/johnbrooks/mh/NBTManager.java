package net.johnbrooks.mh;

import net.johnbrooks.mh.items.CaptureEgg;
import net.minecraft.server.v1_13_R1.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NBTManager {
    public enum ListType {
        END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY;
    }

    public static LivingEntity spawnEntityFromNBTData(ItemStack spawnItem, Location target) {
        if (spawnItem != null) {
            net.minecraft.server.v1_13_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(spawnItem);
            if (nmsStack.hasTag()) {
                NBTTagCompound compound = nmsStack.getTag();
                NBTTagCompound entityDetails = compound.getCompound("tag");

                String entityType = entityDetails.getString("entity type");

                LivingEntity livingEntity = (LivingEntity) target.getWorld().spawnEntity(target.clone().add(0, 1f, 0), EntityType.valueOf(entityType));
                applyNBTDataToEntity(livingEntity, entityDetails);

                return livingEntity;
            }
            else
                Main.logger.warning("Spawn Item does not have any NBT Tags.");
        }
        else
            Main.logger.warning("NULL spawn item passed to #spawnEntityFromNBTData().");

        return null;
    }

    private static void applyNBTDataToEntity(LivingEntity livingEntity, NBTTagCompound entityDetails) {
        if (entityDetails.hasKey("custom name"))
            livingEntity.setCustomName(entityDetails.getString("custom name"));
        livingEntity.setAI(entityDetails.getBoolean("ai"));
        livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(entityDetails.getDouble("max health"));
        livingEntity.setHealth(entityDetails.getDouble("health"));
        livingEntity.setGlowing(entityDetails.getBoolean("glowing"));

        NBTTagList potionEffectList = entityDetails.getList("potion effects", ListType.COMPOUND.ordinal());
        for (int i = 0; i < potionEffectList.size(); i++) {
            NBTTagCompound potionEffectCompound = potionEffectList.getCompound(i);

            int duration = potionEffectCompound.getInt("duration");
            int amplifier = potionEffectCompound.getInt("amplifier");
            int colorRed = -1;
            int colorGreen = -1;
            int colorBlue = -1;
            if (potionEffectCompound.hasKey("color red")) {
                colorRed = potionEffectCompound.getInt("color red");
                colorGreen = potionEffectCompound.getInt("color green");
                colorBlue = potionEffectCompound.getInt("color blue");
            }
            String type = potionEffectCompound.getString("type");
            boolean hasParticles = potionEffectCompound.getBoolean("particles");
            boolean isAmbient = potionEffectCompound.getBoolean("ambient");

            PotionEffectType potionEffectType = PotionEffectType.getByName(type);
            Color color = null;
            if (colorRed != -1 && colorBlue != -1 && colorGreen != -1)
                color = Color.fromRGB(colorRed, colorGreen, colorBlue);

            PotionEffect potionEffect;
            if (color != null)
                potionEffect = new PotionEffect(potionEffectType, duration, amplifier, isAmbient, hasParticles, color);
            else
                potionEffect = new PotionEffect(potionEffectType, duration, amplifier, isAmbient, hasParticles);
            livingEntity.addPotionEffect(potionEffect);
        }

        if (livingEntity instanceof Ageable) {
            Ageable ageable = (Ageable) livingEntity;
            ageable.setAge(entityDetails.getInt("age"));
        }

        if (livingEntity instanceof Tameable) {
            Tameable tameable = (Tameable) livingEntity;
            tameable.setTamed(entityDetails.getBoolean("tamed"));
            if (tameable.isTamed()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(entityDetails.getString("owner")));
                if (offlinePlayer != null)
                    tameable.setOwner(offlinePlayer);
            }
        }

        if (livingEntity instanceof Wolf) {
            // Is a wolf
            Wolf wolf = (Wolf) livingEntity;
            wolf.setSitting(entityDetails.getBoolean("is sitting"));
            wolf.setAngry(entityDetails.getBoolean("is angry"));
            boolean tamed = entityDetails.getBoolean("is tamed");
            wolf.setTamed(tamed);

            if (tamed)
            {
                String ownerUUID = entityDetails.getString("owner");
                String color = entityDetails.getString("color");
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(ownerUUID));
                if (offlinePlayer != null)
                {
                    wolf.setOwner(offlinePlayer);
                    wolf.setCollarColor(DyeColor.valueOf(color));
                }
            }
        } else if (livingEntity instanceof Pig) {
            Pig pig = (Pig) livingEntity;
            pig.setSaddle(entityDetails.getBoolean("saddled"));
        } else if (livingEntity instanceof Sheep) {
            Sheep sheep = (Sheep) livingEntity;
            sheep.setSheared(entityDetails.getBoolean("sheared"));
            sheep.setColor(DyeColor.valueOf(entityDetails.getString("color")));
        } else if (livingEntity instanceof Ocelot) {
            Ocelot ocelot = (Ocelot) livingEntity;
            ocelot.setTamed(entityDetails.getBoolean("tamed"));
            ocelot.setSitting(entityDetails.getBoolean("sitting"));
            if (ocelot.isTamed()) {
                ocelot.setCatType(Ocelot.Type.valueOf(entityDetails.getString("cat type")));
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(entityDetails.getString("owner")));
                if (offlinePlayer != null)
                    ocelot.setOwner(offlinePlayer);
            }
        } else if (livingEntity instanceof Rabbit) {
            Rabbit rabbit = (Rabbit) livingEntity;
            rabbit.setRabbitType(Rabbit.Type.valueOf(entityDetails.getString("rabbit type")));
        } else if (livingEntity instanceof AbstractHorse) {
            AbstractHorse abstractHorse = (AbstractHorse) livingEntity;
            abstractHorse.setJumpStrength(entityDetails.getDouble("jump strength"));
            abstractHorse.setTamed(entityDetails.getBoolean("tamed"));
            abstractHorse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(entityDetails.getDouble("speed"));
            //TODO: INVENTORY CONTENTS
        }
        else if (livingEntity instanceof Villager) {
            //1) Get basic villager data.
            Villager villager = (Villager) livingEntity;
            villager.setProfession(Villager.Profession.valueOf(entityDetails.getString("profession")));
            villager.setRiches(entityDetails.getInt("riches"));

            //2) Grab the recipe list.
            NBTTagList recipeList = entityDetails.getList("recipes", ListType.COMPOUND.ordinal());

            //3) Prepare an ArrayList for recipe list that will be stored in Villager.
            List<org.bukkit.inventory.MerchantRecipe> merchantRecipeList = new ArrayList<>();
            for (int i = 0; i < recipeList.size(); i++)
            {
                //4) Parse the recipe list.
                NBTTagCompound recipeCompound = recipeList.getCompound(i);
                int uses = recipeCompound.getInt("uses");
                int maxUses = recipeCompound.getInt("max uses");
                boolean experienceReward = recipeCompound.getBoolean("experience reward");
                String[] resultString = recipeCompound.getString("result").split("\\.");
                NBTTagCompound resultTags = recipeCompound.getCompound("result tags");
                NBTTagList materialsAndAmount = recipeCompound.getList("materials", ListType.STRING.ordinal());
                NBTTagList tagList = recipeCompound.getList("tags", ListType.COMPOUND.ordinal());

                //5) Set the resulted item stack to its proper NBT tags.
                ItemStack resultItemStack = new ItemStack(Material.valueOf(resultString[0]), Integer.parseInt(resultString[1]));
                net.minecraft.server.v1_13_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(resultItemStack);
                nmsStack.setTag(resultTags);
                resultItemStack = CraftItemStack.asBukkitCopy(nmsStack);

                //6) Register the recipes.
                org.bukkit.inventory.MerchantRecipe merchantRecipe = new org.bukkit.inventory.MerchantRecipe(resultItemStack, maxUses);
                merchantRecipe.setUses(uses);
                merchantRecipe.setExperienceReward(experienceReward);
                List<ItemStack> ingredients = new ArrayList<>();
                for (int j = 0; j < materialsAndAmount.size(); j++)
                {
                    String[] ingredient = materialsAndAmount.getString(j).split("\\.");
                    NBTTagCompound tags = tagList.getCompound(j);
                    ItemStack itemStack = new ItemStack(Material.valueOf(ingredient[0]), Integer.parseInt(ingredient[1]));
                    net.minecraft.server.v1_13_R1.ItemStack nmsIngredientStack = CraftItemStack.asNMSCopy(itemStack);
                    nmsIngredientStack.setTag(tags);
                    itemStack = CraftItemStack.asBukkitCopy(nmsIngredientStack);
                    ingredients.add(itemStack);
                }
                merchantRecipe.setIngredients(ingredients);
                merchantRecipeList.add(merchantRecipe);
            }

            //4) Input the recipe list into the villager
            villager.setRecipes(merchantRecipeList);
        } else if (livingEntity instanceof Creeper) {
            ((Creeper) livingEntity).setPowered(entityDetails.getBoolean("charged"));
        } else if (livingEntity instanceof Slime) {
            ((Slime) livingEntity).setSize(entityDetails.getInt("size"));
        } else if (livingEntity instanceof ZombieVillager) {
            ZombieVillager zombieVillager = (ZombieVillager) livingEntity;
            zombieVillager.setVillagerProfession(Villager.Profession.valueOf(entityDetails.getString("profession")));
        } else if (livingEntity instanceof Parrot) {
            ((Parrot) livingEntity).setVariant(Parrot.Variant.valueOf(entityDetails.getString("variant")));
        } else if (livingEntity instanceof Llama) {
            Integer strength = entityDetails.getInt("strength");
            Llama.Color color = Llama.Color.valueOf(entityDetails.getString("color"));


            Llama llama = (Llama) livingEntity;
            llama.setStrength(strength);
            llama.setColor(color);
        }

        if (livingEntity instanceof InventoryHolder) {
            InventoryHolder inventoryHolder = (InventoryHolder) livingEntity;

            //1) Declare storage for item stack material, slot, and NBT tags
            NBTTagList tagList = entityDetails.getList("inventory nbt tags", ListType.COMPOUND.ordinal());
            NBTTagList materialList = entityDetails.getList("inventory materials", ListType.STRING.ordinal());

            for (int i = 0; i < materialList.size(); i++)
            {
                //2) Load NBT tag data and material type
                String[] materialElements = materialList.getString(i).split("\\.");
                String materialName = materialElements[0];
                int slot = Integer.parseInt(materialElements[1]);
                NBTTagCompound tag = tagList.getCompound(i);

                //3) Create item stack and attach NBT tag to it.
                ItemStack itemStack = new ItemStack(Material.valueOf(materialName));
                if (tag != new NBTTagCompound())
                {
                    net.minecraft.server.v1_13_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
                    nmsStack.setTag(tag);
                    itemStack = CraftItemStack.asBukkitCopy(nmsStack);
                }
                //4) Place in inventory at correct slot number.
                inventoryHolder.getInventory().setItem(slot, itemStack);
            }
        }

        // horse types
        if (livingEntity instanceof Llama) {
            Llama abstractHorse = (Llama) livingEntity;
            Llama.Color color = Llama.Color.valueOf(entityDetails.getString("color"));
            Integer strength = entityDetails.getInt("strength");
            abstractHorse.setColor(color);
            abstractHorse.setStrength(strength);
        } else if (livingEntity instanceof Horse) {
            Horse horse = (Horse) livingEntity;
            Horse.Color color = Horse.Color.valueOf(entityDetails.getString("color"));
            Horse.Style style = Horse.Style.valueOf(entityDetails.getString("style"));
            horse.setColor(color);
            horse.setStyle(style);
        }

    }

    public static ItemStack castEntityDataToItemStackNBT(ItemStack itemStack, LivingEntity livingEntity) {
        //2) Figure entity name
        String entityName;
        if (livingEntity.getCustomName() == null) {
            entityName = livingEntity.getType().name().replace("_", " ").toLowerCase();
            entityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
        }
        else
            entityName = livingEntity.getCustomName();

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(CaptureEgg.TITLE_PREFIX + entityName);
        itemStack.setItemMeta(itemMeta);

        net.minecraft.server.v1_13_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();

        //2) Gather capture data

        NBTTagCompound entityDetails = generateNBTTagCompound(tagCompound, livingEntity);
        tagCompound.set("tag", entityDetails);

        NBTTagCompound display = tagCompound.hasKey("display") ? tagCompound.getCompound("display") : new NBTTagCompound();

        //3) What is the name of the item stack
        //TODO: Re-enable this when NBT Tags are finished in Spigot. SPIGOT BUG
        //display.set("Name", new NBTTagString(CaptureEgg.TITLE_PREFIX + entityName));
        //4) Setup lore
        NBTTagList list = new NBTTagList();
        list.add(new NBTTagString(ChatColor.AQUA + "Creature Type: " + ChatColor.YELLOW + livingEntity.getType().name()));
        //5) If the health/max-health is an integer, don't write it out to be a double.
        String healthData;
        if (round(livingEntity.getHealth(), 1) == (int) livingEntity.getHealth())
            healthData = String.valueOf((int) livingEntity.getHealth());
        else
            healthData = String.valueOf(round(livingEntity.getHealth(), 1));
        String maxHealthData;
        if (round(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), 1) == (int) livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue())
            maxHealthData = String.valueOf((int) livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        else
            maxHealthData = String.valueOf(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());

        list.add(new NBTTagString(ChatColor.AQUA + "Health: " + ChatColor.YELLOW + healthData + "/" + maxHealthData));

        // if baby, add to lore
        if (livingEntity instanceof Ageable) {
            Ageable ageable = (Ageable) livingEntity;
            if (!ageable.isAdult())
                list.add(new NBTTagString(ChatColor.AQUA + "Age: " + ChatColor.YELLOW + "Baby"));
        }

        // if tamed
        if (livingEntity instanceof Tameable) {
            Tameable tameable = (Tameable) livingEntity;
            list.add(new NBTTagString(ChatColor.AQUA + "Tamed: " + ChatColor.YELLOW + (tameable.isTamed() ? "Yes" : "No")));
        }

        if (livingEntity instanceof Parrot) {
            Parrot parrot = (Parrot) livingEntity;
            list.add(new NBTTagString(ChatColor.AQUA + "Color: " + ChatColor.YELLOW + parrot.getVariant().name()));
        }

        //6) If potion effects active, display them
        NBTTagList potionEffectList = entityDetails.getList("potion effects", ListType.COMPOUND.ordinal());
        if (potionEffectList.size() > 0) {
            for (int i = 0; i < potionEffectList.size(); i++) {
                NBTTagCompound potionEffectComponent = potionEffectList.getCompound(i);
                int duration = potionEffectComponent.getInt("duration");
                String type = potionEffectComponent.getString("type").replace("_", " ").toLowerCase();
                type = type.substring(0, 1).toUpperCase() + type.substring(1);
                list.add(new NBTTagString(ChatColor.DARK_PURPLE + type + " for " + (duration / 20) + " seconds"));
            }
        }

        display.set("Lore", list);
        tagCompound.set("display", display);

        //) Package and convert
        nmsStack.setTag(tagCompound);
        itemStack = CraftItemStack.asBukkitCopy(nmsStack);

        return itemStack;
    }

    private static NBTTagCompound generateNBTTagCompound(NBTTagCompound compound, LivingEntity livingEntity) {
        NBTTagCompound entityDetails = compound.getCompound("tag");
        // General entity data
        if (livingEntity.getCustomName() != null)
            entityDetails.set("custom name", new NBTTagString(livingEntity.getCustomName()));
        entityDetails.set("entity type", new NBTTagString(livingEntity.getType().name()));
        entityDetails.setBoolean("ai", livingEntity.hasAI());
        entityDetails.set("health", new NBTTagDouble(livingEntity.getHealth()));
        entityDetails.set("max health", new NBTTagDouble(livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()));
        entityDetails.setBoolean("glowing", livingEntity.isGlowing());

        NBTTagList potionEffectList = new NBTTagList();
        //TODO: POTION EFFECTS
        for (PotionEffect potionEffect : livingEntity.getActivePotionEffects())
        {
            NBTTagCompound potionEffectCompound = new NBTTagCompound();
            potionEffectCompound.setInt("duration", potionEffect.getDuration());
            potionEffectCompound.setInt("amplifier", potionEffect.getAmplifier());
            if (potionEffect.getColor() != null)
            {
                potionEffectCompound.setInt("color red", potionEffect.getColor().getRed());
                potionEffectCompound.setInt("color green", potionEffect.getColor().getGreen());
                potionEffectCompound.setInt("color blue", potionEffect.getColor().getBlue());
            }
            potionEffectCompound.setString("type", potionEffect.getType().getName());
            potionEffectCompound.setBoolean("ambient", potionEffect.isAmbient());
            potionEffectCompound.setBoolean("particles", potionEffect.hasParticles());

            potionEffectList.add(potionEffectCompound);
        }
        entityDetails.set("potion effects", potionEffectList);

        if (livingEntity instanceof Ageable) {
            Ageable ageable = (Ageable) livingEntity;
            entityDetails.setInt("age", ageable.getAge());
        }
        if (livingEntity instanceof InventoryHolder) {
            InventoryHolder inventoryHolder = (InventoryHolder) livingEntity;

            NBTTagList tagList = new NBTTagList();
            NBTTagList materialList = new NBTTagList();
            for (int i = 0; i < inventoryHolder.getInventory().getContents().length; i++)
            {
                ItemStack itemStack = inventoryHolder.getInventory().getContents()[i];
                if (itemStack != null)
                {
                    net.minecraft.server.v1_13_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
                    materialList.add(new NBTTagString(itemStack.getType().name() + "." + i));
                    if (nmsStack.hasTag())
                        tagList.add(nmsStack.getTag());
                    else
                        tagList.add(new NBTTagCompound());
                }
            }

            entityDetails.set("inventory materials", materialList);
            entityDetails.set("inventory nbt tags", tagList);
        }

        // Wolf
        if (livingEntity instanceof Wolf) {
            // Basic wolf data
            Wolf wolf = (Wolf) livingEntity;
            boolean isAngry = wolf.isAngry();
            boolean isSitting = wolf.isSitting();
            boolean isTamed = wolf.isTamed();

            entityDetails.setBoolean("is angry", isAngry);
            entityDetails.setBoolean("is sitting", isSitting);
            entityDetails.setBoolean("is tamed", isTamed);
            if (isTamed) {
                // Tamed wolf data
                String color = wolf.getCollarColor().name();
                String ownerUUID = wolf.getOwner().getUniqueId().toString();

                entityDetails.set("color", new NBTTagString(color));
                entityDetails.set("owner", new NBTTagString(ownerUUID));
            }
        } else if (livingEntity instanceof Sheep) {
            Sheep sheep = (Sheep) livingEntity;
            boolean sheared = sheep.isSheared();
            String color = sheep.getColor().name();

            entityDetails.set("color", new NBTTagString(color));
            entityDetails.setBoolean("sheared", sheared);
        } else if (livingEntity instanceof Pig) {
            Pig pig = (Pig) livingEntity;

            boolean saddled = pig.hasSaddle();
            entityDetails.setBoolean("saddled", saddled);
        } else if (livingEntity instanceof Ocelot) {
            Ocelot ocelot = (Ocelot) livingEntity;
            entityDetails.setBoolean("tamed", ocelot.isTamed());
            entityDetails.setBoolean("sitting", ocelot.isSitting());
            if (ocelot.isTamed()) {
                String catType = ocelot.getCatType().name();
                String ownerUUID = ocelot.getOwner().getUniqueId().toString();
                entityDetails.setString("cat type", catType);
                entityDetails.setString("owner", ownerUUID);
            }
        } else if (livingEntity instanceof Rabbit) {
            entityDetails.setString("rabbit type", ((Rabbit) livingEntity).getRabbitType().name());
        } else if (livingEntity instanceof AbstractHorse) {
            AbstractHorse abstractHorse = (AbstractHorse) livingEntity;
            double jumpStrength = abstractHorse.getJumpStrength();
            double speed = livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();

            entityDetails.setDouble("jump strength", jumpStrength);
            entityDetails.setDouble("speed", speed);

        } else if (livingEntity instanceof Villager) {
            Villager villager = (Villager) livingEntity;
            String profession = villager.getProfession().name();
            int riches = villager.getRiches();

            NBTTagList recipeList = new NBTTagList();
            for (org.bukkit.inventory.MerchantRecipe recipe : villager.getRecipes()) {
                List<ItemStack> ingredients = recipe.getIngredients();
                // Store the materials and amounts
                NBTTagList materialsAndAmount = new NBTTagList(); // Holds materials and amount separated by "."
                // Store the tags
                NBTTagList itemStackTags = new NBTTagList();
                for (ItemStack itemStack : ingredients) {
                    materialsAndAmount.add(new NBTTagString(itemStack.getType().name() + "." + itemStack.getAmount()));

                    net.minecraft.server.v1_13_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
                    NBTTagCompound itemStackCompound = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
                    itemStackTags.add(itemStackCompound);
                }
                int uses = recipe.getUses();
                int maxUses = recipe.getMaxUses();
                boolean experienceReward = recipe.hasExperienceReward();

                net.minecraft.server.v1_13_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(recipe.getResult());
                NBTTagCompound resultTags = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();

                NBTTagCompound recipeCompound = new NBTTagCompound();
                recipeCompound.setInt("uses", uses);
                recipeCompound.setInt("max uses", maxUses);
                recipeCompound.setBoolean("experience reward", experienceReward);
                recipeCompound.setString("result", recipe.getResult().getType().name() + "." + recipe.getResult().getAmount());
                recipeCompound.set("result tags", resultTags);
                recipeCompound.set("materials", materialsAndAmount);
                recipeCompound.set("tags", itemStackTags);

                recipeList.add(recipeCompound);
            }

            entityDetails.setString("profession", profession);
            entityDetails.setInt("riches", riches);
            entityDetails.set("recipes", recipeList);
        } else if (livingEntity instanceof Creeper) {
            Creeper creeper = (Creeper) livingEntity;
            entityDetails.setBoolean("charged", creeper.isPowered());
        } else if (livingEntity instanceof Slime) {
            Slime slime = (Slime) livingEntity;
            entityDetails.setInt("size", slime.getSize());
        } else if (livingEntity instanceof ZombieVillager) {
            ZombieVillager zombieVillager = (ZombieVillager) livingEntity;
            String profession = zombieVillager.getVillagerProfession().name();
            entityDetails.setString("profession", profession);
        } else if (livingEntity instanceof Parrot) {
            Parrot parrot = (Parrot) livingEntity;
            Parrot.Variant color = parrot.getVariant();
            entityDetails.setString("variant", color.name());
        }

        if (livingEntity instanceof Tameable) {
            Tameable tameable = (Tameable) livingEntity;
            boolean tamed = tameable.isTamed();
            entityDetails.setBoolean("tamed", tamed);
            if (tamed) {
                String ownerUUID = tameable.getOwner().getUniqueId().toString();
                entityDetails.setString("owner", ownerUUID);
            }
        }

        // horse types
        if (livingEntity instanceof Llama) {
            Llama abstractHorse = (Llama) livingEntity;
            String color = abstractHorse.getColor().name();
            Integer strength = abstractHorse.getStrength();
            entityDetails.setString("color", color);
            entityDetails.setInt("strength", strength);
        } else if (livingEntity instanceof Horse) {
            Horse abstractHorse = (Horse) livingEntity;
            String color = abstractHorse.getColor().name();
            String style = abstractHorse.getStyle().name();
            entityDetails.setString("color", color);
            entityDetails.setString("style", style);
        }

        return entityDetails;
    }

    private static double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}
