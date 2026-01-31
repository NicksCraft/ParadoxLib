# ParadoxLib

A library for Slimefun4 Addons that adds a lot of useful events, classes, and utilities.

Inspiration taken from [InfinityLib](https://github.com/Mooy1/InfinityLib) and [ExtraUtils](https://github.com/Slimefun-Addon-Community/ExtraUtils), mostly of the code is from the InfinityLib

[![Release](https://jitpack.io/v/NicksCraft/ParadoxLib.svg?style=flat-square)](https://jitpack.io/#NicksCraft/ParadoxLib)

# Packages & Features

## Core

<b>ParadoxAddon</b>: An implementation of JavaPlugin
which you will need to extend for many of the other features to work.
It provides multiple utility methods and does some basic setup for you.

<b>AddonConfig</b>: is an implementation of YamlConfiguration
which makes comments available in the user's config
and provides utility methods such as getting a value from within a range
and removing unused/old keys from the user's config.

## Events

<b>ArmorEquipEvent</b>: Gets called when a player equipping/unequipping armor piece

<b>PlayerBreakOreEvent</b>: Gets called when a player break a ore

<b>PlayerBreakWoodEvent</b>: Gets called when a player break a wood

<b>PlayerFishItemEvent</b>: Gets called when a player fish a item

<b>PlayerStrippedLogEvent</b>: Gets called when a player stripped a log/wood

## Common

<b>Cooldowns</b>: A utility object for keeping track of cool downs of players/uuids

<b>PersistentType</b>: Contains some PersistentDataTypes for
ItemStack's, ItemStack Array's, Locations, and String Arrays.
Also provides a constructor for PersistentDataType that uses lambda parameters.

<b>Events</b>: Contains static utility methods for registering listeners, creating handlers, and calling events

<b>Scheduler</b>: Contains static utility methods for running and repeating tasks

## Commands

<b>AddonCommand</b>: allows you to add commands easily with a parent-child structure,
so you could have a command with a sub command which has a sub command.
It also adds some default commands such as an addon info, aliases, and help command.

## Enchants

<b>GlowEnchant</b>: A custom enchantment called GlowEnchant(From ExtraUtils)

## Machines

<b>ParadoxInventoryBlock</b>: A SlimefunItem with a menu, providing overridable methods for setting up the menu

<b>ParadoxTickingBlock</b>: A ParadoxInventoryBlock with slimefun ticker

<b>ParadoxMachineBlock</b>: A ParadoxTickingBlock which implements EnergyNetComponent and provides a process method

<b>ParadoxGeneratorBlock</b>: A ParadoxTickingBlock which implements EnergyNetComponent and provides a generate method

## Utils

<b>SkullTexture</b>: A class to get a custom skull texture even if its not in textures.minecraft.com

## Future Additions

<b>Translation Utility</b>: Some sort of easy way to create translatable strings for your addon's and paradoxlibs's strings

# How to use

First you need to add ParadoxLib to the `dependencies` section in your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.NicksCraft</groupId>
    <artifactId>ParadoxLib</artifactId>
    <version>SPECIFY VERSION HERE</version>
    <scope>compile</scope>
</dependency>
```

Then you need to relocate it into your own package so that it doesn't conflict with other addon's classes.

Under the `build` section in your `pom.xml`, you should have the following:

```xml
<plugins>
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.2.4</version>
        <configuration>
            <!-- This will exclude any unused classes from libraries to reduce file size, not required -->
            <minimizeJar>true</minimizeJar>
            <relocations>
                <!-- This is the relocation, make sure to replace the package name, REQUIRED -->
                <relocation>
                    <pattern>com.ncoder.paradoxlib</pattern>
                    <shadedPattern>YOUR.MAIN.PACKAGE.HERE.paradoxlib</shadedPattern>
                </relocation>
            </relocations>
            <filters>
                <filter>
                    <artifact>*:*</artifact>
                    <excludes>
                        <exclude>META-INF/*</exclude>
                    </excludes>
                </filter>
            </filters>
        </configuration>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>shade</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>
```

Then change your main plugin class to extend `ParadoxAddon` and implement the constructor.
You will need to use `Enable()` and `Disable()` instead of `onEnable()` and `onDisable`.
Make sure you don't call `super.onEnable/Disable`.
Your updater and config setup is now handled, make sure to test that it's working though!
