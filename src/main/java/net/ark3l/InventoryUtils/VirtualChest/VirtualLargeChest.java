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
    along with GiftPost or InventoryUtils.  If not, see <http://www.gnu.org/licenses/>.*/

package net.ark3l.InventoryUtils.VirtualChest;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryLargeChest;
import net.minecraft.server.ItemStack;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;

/**
 * @author Balor (aka Antoine Aflalo). Edited by Arkel
 */
public class VirtualLargeChest extends VirtualChest {

    protected final TileEntityVirtualChest subChest2;
    protected InventoryLargeChest lc;

    public VirtualLargeChest(String chestName) {
        super(chestName);
        subChest2 = new TileEntityVirtualChest();
        subChest2.setName(chestName);
        lc = new InventoryLargeChest(chestName, chest, subChest2);
    }

    /**
     * Open the chest for the player
     * @param player The player to open the chest for
     */
    @Override
    public void openChest(Player player) {
        EntityPlayer eh = ((CraftPlayer) player).getHandle();
        eh.a(lc);
    }

    @Override
    public boolean addItemStack(ItemStack itemStack) {
        if (isFull())
            return false;
        if (!super.addItemStack(itemStack))
            return subChest2.addItemStack(itemStack);
        return true;
    }

    @Override
    public org.bukkit.inventory.ItemStack getItem(int index) {
        return new CraftItemStack(lc.getItem(index));
    }

    @Override
    public org.bukkit.inventory.ItemStack[] getContents() {
        org.bukkit.inventory.ItemStack[] items = new org.bukkit.inventory.ItemStack[lc.getSize()];
        net.minecraft.server.ItemStack[] mcItems = lc.getContents();

        for (int i = 0; i < mcItems.length; i++) {
            items[i] = mcItems[i] == null ? null : new CraftItemStack(mcItems[i]);
        }

        return items;
    }

    @Override
    public boolean isFull() {
        return super.isFull() && subChest2.isFull();
    }

    @Override
    protected int firstEmpty() {
        int firstFree = super.firstEmpty();
        if (firstFree == -1)
            return subChest2.firstFree() + chest.size();
        else
            return firstFree;
    }

    @Override
    protected int getMaxItemStack() {
        return lc.getMaxStackSize();
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() && subChest2.isEmpty();
    }

    @Override
    public int usedCases() {
        return (chest.size() + subChest2.size()) - leftCases();
    }

    @Override
    public void emptyChest() {
        super.emptyChest();
        subChest2.emptyChest();
    }

    @Override
    public int leftCases() {
        return chest.emptyCasesLeft() + subChest2.emptyCasesLeft();
    }

    @Override
    public ItemStack[] getMcContents() {
        return lc.getContents();
    }

    @Override
    public boolean removeItemStack(ItemStack itemStack) {
        if (!super.removeItemStack(itemStack)) {
            for (int i = 0; i < subChest2.getContents().length; i++)
                if (subChest2.getContents()[i].equals(itemStack)) {
                    subChest2.removeItemStack(i);
                    return true;
                }
            return false;
        }
        return true;
    }

    @Override
    public void removeItemStack(int i) {
        if (i > chest.getSize())
            subChest2.removeItemStack(i - chest.getSize());
        else
            super.removeItemStack(i);
    }

    @Override
    public ItemStack getItemStack(int slot) {
        return lc.getItem(slot);
    }

    @Override
    public void setName(String name) {
        lc = new InventoryLargeChest(name, chest, subChest2);
    }

    @Override
    public void setItemStack(int slot, ItemStack itemStack) {
        lc.setItem(slot, itemStack);
    }

    @Override
    public VirtualLargeChest clone() {
        try {
            VirtualLargeChest result = (VirtualLargeChest) super.clone();
            return result;
        } catch (Exception e) {
            throw new AssertionError();
        }
    }

    public org.bukkit.inventory.ItemStack[] getUpperContents() {
        org.bukkit.inventory.ItemStack[] items = new org.bukkit.inventory.ItemStack[chest.getSize()];
        net.minecraft.server.ItemStack[] mcItems = chest.getContents();

        for (int i = 0; i < mcItems.length; i++) {
            items[i] = mcItems[i] == null ? null : new CraftItemStack(mcItems[i]);
        }

        return items;
    }

    public org.bukkit.inventory.ItemStack[] getLowerContents() {
        org.bukkit.inventory.ItemStack[] items = new org.bukkit.inventory.ItemStack[subChest2.getSize()];
        net.minecraft.server.ItemStack[] mcItems = subChest2.getContents();

        for (int i = 0; i < mcItems.length; i++) {
            items[i] = mcItems[i] == null ? null : new CraftItemStack(mcItems[i]);
        }

        return items;
    }

}
