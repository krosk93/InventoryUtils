package net.ark3l.InventoryUtils.Persistence;

import net.ark3l.InventoryUtils.VirtualChest.VirtualChest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.inventory.SpoutItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * @author Arkel
 */
public class PersistenceManager {
    
    private Plugin plugin;
    private Logger log = Logger.getLogger("Minecraft");
    
    public PersistenceManager(Plugin instance) {
             plugin = instance;
    }
    
    /**
     * Get the players Items on the specified network
     *
     * @param file The file to retrieve the items from
     * @return A list containing the Items
     */
    public List<SpoutItemStack> getItems(File file) {

        // Create any missing stuff
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayList<SpoutItemStack> itemStacks = new ArrayList<SpoutItemStack>();
        List<String> lines = getLinesFromFile(file);

        for (String line : lines) {
            itemStacks.add(stringToItemStack(line));
        }

        return itemStacks;
    }


    /**
     * Saves the virtualchest to file
     * @param chest The VirtualChest to save
     * @param file The file to save it too
     */
    public void saveChest(VirtualChest chest, File file) {
         saveItems(file, chest.getContents());
    }

    /**
     * Save the items for the player on the specified network
     *
     * @param file The file to save the items to
     * @param items      An array containing the items
     */
    public void saveItems(File file, ItemStack[] items) {

        // Create the folder for the network if it doesn't exist
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // Wipe the file and save a new one in its place
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            List<String> itemsAsStrings = new ArrayList<String>();
            for (ItemStack i : items) {
                itemsAsStrings.add(itemStackToString(i));
            }
            writeLinesToFile(itemsAsStrings, file);
        } catch (IOException e) {
            log.severe("Error creating file " + file.getPath());
            e.printStackTrace();
        }

    }

    /**
     * Writes a line to a file
     *
     * @param line The line to be written
     * @param file The file to write to
     */
    protected void writeLineToFile(String line, File file) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(line);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            log.severe("Error writing to file " + file.getPath());
            e.printStackTrace();
        }
    }

    /**
     * Write several lines to a file
     *
     * @param lines A list containing the lines to be written
     * @param file
     */
    protected void writeLinesToFile(List<String> lines, File file) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            log.severe("Error writing to file " + file.getPath());
            e.printStackTrace();
        }
    }

    /**
     * Gets all the lines from a file
     *
     * @param file The file to retrieve the lines from
     * @return A list containing the lines retrieved from the file
     */
    protected List<String> getLinesFromFile(File file) {
        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file))));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                if (!strLine.isEmpty())
                    lines.add(strLine);
            }

            br.close();
        } catch (Exception e) {
            log.severe("Error reading file " + file.getPath());
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * Serializes an ItemStack, turning it into a series of strings seperated by semi-colons
     *
     * @param itemstack The ItemStack to serialize
     * @return A string in the format ITEMID;AMOUNT;DATA;DURABILITY;ENCHANTMENTID;ENCHANTMENTLEVEL;ENCHA....
     */
    protected String itemStackToString(ItemStack itemstack) {
        StringBuilder sb = new StringBuilder();

        if (itemstack != null) {
            sb.append(itemstack.getTypeId());
            sb.append(";");
            sb.append(itemstack.getAmount());
            sb.append(";");
            sb.append(itemstack.getData().getData());
            sb.append(";");
            sb.append(itemstack.getDurability());
            for (Map.Entry<Enchantment, Integer> enchantmentEntry : itemstack.getEnchantments().entrySet()) {
                Map.Entry pairs = (Map.Entry) enchantmentEntry;
                Enchantment ench = (Enchantment) pairs.getKey();
                int level = (Integer) pairs.getValue();
                sb.append(";").append(ench.getId()).append(";").append(level);
            }
        }

        return sb.toString();
    }

    /**
     * Converts a string into a SpoutItemStack
     *
     * @param string A string in the format ITEMID;AMOUNT;DATA;DURABILITY;ENCHANTMENTID;ENCHANTMENTLEVEL;ENCHA....
     * @return The ItemStack produced by the string
     */
    protected SpoutItemStack stringToItemStack(String string) {
        Scanner sc = new Scanner(string);
        sc.useDelimiter(";");

        int typeid = sc.nextInt();
        int amount = sc.nextInt();
        byte data = sc.nextByte();
        int durability = sc.nextInt();

        SpoutItemStack itemStack = new SpoutItemStack(typeid, amount, data);

        while (sc.hasNextInt()) {
            int id = sc.nextInt();
            int level = sc.nextInt();
            try {
                itemStack.addEnchantment(Enchantment.getById(id), level);
            } catch (IllegalArgumentException e) {
                itemStack.addUnsafeEnchantment(Enchantment.getById(id), level);
            }
        }

        itemStack.setDurability((short) durability);

        return itemStack;
    }
    
}
