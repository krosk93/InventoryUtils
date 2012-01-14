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
    along with GiftPost or InventoryUtils.  If not, see <http://www.gnu.org/licenses/>.*/


import net.minecraft.server.EntityHuman;
import net.minecraft.server.ItemStack;
import net.minecraft.server.TileEntityChest;

import java.util.ArrayDeque;
import java.util.Queue;

public class TileEntityVirtualChest extends TileEntityChest {

    private String name = "Chest";
    private Queue<Integer> emptyCases;

    public TileEntityVirtualChest() {
        super();
        initEmptyCases();
    }

    private void initEmptyCases() {
        emptyCases = new ArrayDeque<Integer>(getSize());
        for (int i = 0; i < getSize(); i++)
            emptyCases.add(i);
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return if the chest is full
     *
     * @return
     */
    public boolean isFull() {
        return emptyCases.isEmpty();
    }

    /**
     * Return if the chest is empty
     *
     * @return
     */
    public boolean isEmpty() {
        return emptyCases.size() == getSize();
    }

    /**
     * return the number of emptyCases
     *
     * @return
     */
    public int emptyCasesLeft() {
        return emptyCases.size();
    }

    /**
     * Alias to q_()
     *
     * @return
     */
    public int size() {
        return getSize();
    }

    /**
     * Look for the first empty case in the chest to add the stack.
     *
     * @param itemstack
     * @return
     */
    public boolean addItemStack(ItemStack itemstack) {
        Integer i = emptyCases.poll();
        if (i == null)
            return false;
        else {
            super.setItem(i, itemstack);
            return true;
        }
    }

    /**
     * Get the first free slot
     * @return The number of the first slot that is empty
     */
    public int firstFree() {
        Integer firstFree = emptyCases.poll();
        return firstFree == null ? -1 : firstFree;
    }

    /**
     * Sets the slot to the given itemstack
     * @param slot The slot number
     * @param itemstack The ItemStack to set it to
     */
    @Override
    public void setItem(int slot, ItemStack itemstack) {
        if (slot >= 0 && slot < getSize()) {
            if (itemstack != null)
                emptyCases.remove(slot);
            else
                emptyCases.add(slot);
            super.setItem(slot, itemstack);
        }
    }

    /**
     * Empties the chest
     */
    public void emptyChest() {
        for (int i = 0; i < this.getContents().length; i++)
            this.getContents()[i] = null;
        initEmptyCases();
    }

    /**
     * Splits the given ItemStack
     * @param i
     * @param j
     * @return
     */
    @Override
    public ItemStack splitStack(int i, int j) {
        ItemStack toReturn = super.splitStack(i, j);
        if (toReturn != null) {
            ItemStack afterSuper[] = this.getContents();
            if (afterSuper[i] == null)
                emptyCases.add(i);
        }

        return toReturn;
    }

    /**
     * Delete the ItemStack at the given slot
     * @param slot The slot to empty
     */
    public void removeItemStack(int slot) {
        this.setItem(slot, null);
    }

    /**
     * Get the name of the inventory
     * @return The name of the chest
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        /*
        * For this proof of concept, we ALWAYS validate the chest. This
        * behavior has not been thoroughly tested, and may cause unexpected
        * results depending on the state of the player.
        *
        * Depending on your purposes, you might want to change this. It would
        * likely be preferable to enforce your business logic outside of this
        * file instead, however.
        */
        return true;
    }

}