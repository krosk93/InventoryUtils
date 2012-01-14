package net.ark3l.InventoryUtils.VirtualChest;
/*
    This file was released as part of GiftPost and redistributed as part of InventoryUtils

    GiftPost and InventoryUtils are free software: you can redistribute it and/or modify
    them under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GiftPost and InventoryUtils are distributed in the hope that they will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GiftPost or InventoryUtils.  If not, see <http://www.gnu.org/licenses/>.
*/

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemStack;
import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * VirtualChest for Bukkit
 *
 * @authors Timberjaw & Balor. Edited by Arkel
 */
public class VirtualChest implements Cloneable {

    protected TileEntityVirtualChest chest;

    /**
     * Construct a VirtualChest
     *
     * @param chestName
     */
    public VirtualChest(String chestName) {
        chest = new TileEntityVirtualChest();
        chest.setName(chestName);
    }

    public VirtualChest(VirtualChest v) {
        this.chest = v.chest;
    }

    /**
     * Open the chest for the player
     * @param player The player to open the chest for
     */
    public void openChest(Player player) {
        EntityPlayer eh = ((CraftPlayer) player).getHandle();
        eh.a(chest);
    }

    /**
     * Add ItemStacks to the chest
     * @param itemStacks The ItemStacks to add
     */
    public void addItemStack(ItemStack[] itemStacks) {
        for(ItemStack itemStack : itemStacks) {
            addItemStack(itemStack);
        }
    }

    /**
     * Adds a single ItemStack to the chest
     * @param itemStack The itemstack to add
     * @return false if the ItemStack could not be added
     */
    public boolean addItemStack(ItemStack itemStack) {
        if (isFull())
            return false;
        return chest.addItemStack(itemStack);
    }

    /**
     * Empty chest
     */
    public void emptyChest() {
        chest.emptyChest();
    }

    /**
     * Check whether the chest is full
     * @return True if the chest is full
     */
    public boolean isFull() {
        return chest.isFull();
    }

    /**
     * Checks whether the chest is empty
     * @return True if the chest is empty
     */
    public boolean isEmpty() {
        return chest.isEmpty();
    }

    /**
     * Get the number of empty (containing no items) cases left
     * @return The number of empty cases
     */
    public int leftCases() {
        return chest.emptyCasesLeft();
    }

    /**
     * Get the number of used (non-empty) cases
     * @return The number of used cases
     */
    public int usedCases() {
        return chest.size() - chest.emptyCasesLeft();
    }

    /**
     * Get the contents of the chest in Notchian form
     * @return Array containing the contents of the chest in Notchian form
     */
    public ItemStack[] getMcContents() {
        return chest.getContents();
    }

    // CraftBukkit Code
    protected int firstPartial(int materialId) {
        org.bukkit.inventory.ItemStack[] inventory = getContents();
        for (int i = 0; i < inventory.length; i++) {
            org.bukkit.inventory.ItemStack item = inventory[i];
            if (item != null && item.getTypeId() == materialId
                    && item.getAmount() < item.getMaxStackSize()) {
                return i;
            }
        }
        return -1;
    }

    protected int firstPartial(Material material) {
        return firstPartial(material.getId());
    }

    protected int firstPartial(org.bukkit.inventory.ItemStack item) {
        org.bukkit.inventory.ItemStack[] inventory = getContents();
        if (item == null) {
            return -1;
        }
        for (int i = 0; i < inventory.length; i++) {
            org.bukkit.inventory.ItemStack cItem = inventory[i];
            if (cItem != null && cItem.getTypeId() == item.getTypeId()
                    && cItem.getAmount() < cItem.getMaxStackSize()
                    && cItem.getDurability() == item.getDurability()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the number of the first free slot in the chest
     * @return The number of the first free slot
     */
    protected int firstEmpty() {
        return chest.firstFree();
    }

    /**
     * Get the max stack size
     * @return The max stack size
     */
    protected int getMaxItemStack() {
        return chest.getMaxStackSize();
    }

    /**
     * Set the index to the chosen Bukkit ItemStack
     * @param index The index to set
     * @param item The item to set it to
     */
    public void setItem(int index, org.bukkit.inventory.ItemStack item) {
        setItemStack(
                index,
                (item == null ? null : CraftItemStack.createNMSItemStack(item)));
    }

    /**
     * Adds Bukkit ItemStacks to the virtual chest
     * @param items the items to add
     * @return A HashMap containing any items that couldn't be added
     */
    public HashMap<Integer, org.bukkit.inventory.ItemStack> addItem(
            org.bukkit.inventory.ItemStack... items) {
        HashMap<Integer, org.bukkit.inventory.ItemStack> leftover = new HashMap<Integer, org.bukkit.inventory.ItemStack>();

        /*
           * TODO: some optimization - Create a 'firstPartial' with a 'fromIndex'
           * - Record the lastPartial per Material
           */

        for (int i = 0; i < items.length; i++) {
            org.bukkit.inventory.ItemStack item = items[i];
            while (true) {
                // Do we already have a stack of it?
                int firstPartial = firstPartial(item);

                // Drat! no partial stack
                if (firstPartial == -1) {
                    // Find a free spot!
                    int firstFree = firstEmpty();

                    if (firstFree == -1) {
                        // No space at all!
                        leftover.put(i, item);
                        break;
                    } else {
                        // More than a single stack!
                        if (item.getAmount() > getMaxItemStack()) {
                            setItem(firstFree, new CraftItemStack(item));
                            item.setAmount(item.getAmount() - getMaxItemStack());
                        } else {
                            // Just store it
                            setItem(firstFree, item);
                            break;
                        }
                    }
                } else {
                    // So, apparently it might only partially fit, well lets do
                    // just that
                    org.bukkit.inventory.ItemStack partialItem = getItem(firstPartial);

                    int amount = item.getAmount();
                    int partialAmount = partialItem.getAmount();
                    int maxAmount = partialItem.getMaxStackSize();

                    // Check if it fully fits
                    if (amount + partialAmount <= maxAmount) {
                        partialItem.setAmount(amount + partialAmount);
                        break;
                    }

                    // It fits partially
                    partialItem.setAmount(maxAmount);
                    item.setAmount(amount + partialAmount - maxAmount);
                }
            }
        }
        return leftover;
    }

    /**
     * Remove items matching the given material ID
     * @param materialId The ID of the material type
     */
    public void remove(int materialId) {
        org.bukkit.inventory.ItemStack[] items = getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getTypeId() == materialId) {
                removeItemStack(i);
            }
        }
    }

    /**
     * Remove items matching the given material
     * @param material The material type to remove
     */
    public void remove(Material material) {
        remove(material.getId());
    }

    /**
     * Remove a Bukkit ItemStack from the VirtualChest
     * @param item The item to remove
     */
    public void remove(ItemStack item) {
        org.bukkit.inventory.ItemStack[] items = getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].equals(item)) {
                removeItemStack(i);
            }
        }
    }

    /**
     * Retrieves a CraftBukkit item from the chest
     * @param index The index to retrieve the item from
     * @return The item
     */
    public org.bukkit.inventory.ItemStack getItem(int index) {
        return new CraftItemStack(chest.getItem(index));
    }

    /**
     * Transform every item to a CraftBukkit item
     * @return An array containing the craftbukkit items
     */
    public org.bukkit.inventory.ItemStack[] getContents() {
        org.bukkit.inventory.ItemStack[] items = new org.bukkit.inventory.ItemStack[chest.getSize()];
        net.minecraft.server.ItemStack[] mcItems = chest.getContents();

        for (int i = 0; i < mcItems.length; i++) {
            items[i] = mcItems[i] == null ? null : new CraftItemStack(mcItems[i]);
        }

        return items;
    }

    // End of CraftBukkit Code

    /**
     * Search for a given itemStack and remove it.
     * @param itemStack The itemstack to remove
     */
    public boolean removeItemStack(ItemStack itemStack) {
        for (int i = 0; i < this.getMcContents().length; i++)
            if (this.getMcContents()[i].equals(itemStack)) {
                chest.removeItemStack(i);
                return true;
            }
        return false;
    }

    public void removeItemStack(int i) {
        chest.removeItemStack(i);
    }

    /**
     * Get the ItemStack at the given slot
     * @param slot
     * @return
     */
    public ItemStack getItemStack(int slot) {
        return chest.getItem(slot);
    }

    /**
     * Set a given itemStack
     * @param slot
     * @param itemStack
     */
    public void setItemStack(int slot, ItemStack itemStack) {
        chest.setItem(slot, itemStack);
    }

    /**
     * Swap 2 slots ItemStacks
     * @param from Slot from
     * @param to Slot too
     */
    public void swapItemStack(int from, int to) {
        ItemStack first = getItemStack(from);
        ItemStack second = getItemStack(to);
        setItemStack(from, second);
        setItemStack(to, first);
    }

    /**
     * Get the name of the chest
     * @return The name of the chest
     */
    public String getName() {
        return this.chest.getName();
    }

    /**
     * Sets the name of the chest
     * @param name
     */
    public void setName(String name) {
        this.chest.setName(name);
    }

    @Override
    public VirtualChest clone() {
        try {
            VirtualChest result = (VirtualChest) super.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}