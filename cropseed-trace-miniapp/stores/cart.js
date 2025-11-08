import { defineStore } from "pinia";
import config from "@/common/config.js";
import {
  getCartList,
  addToCart,
  updateCartQuantity,
  updateCartSelected,
  batchUpdateCartSelected,
  deleteCart,
  batchDeleteCart,
  clearCart as clearCartApi,
} from "@/api/cart.js";

export const useCartStore = defineStore("cart", {
  state: () => ({
    list: [],
    loaded: false,
    loading: false,
  }),
  getters: {
    totalQuantity: (state) =>
      state.list.reduce((sum, item) => sum + (item.quantity || 0), 0),
    selectedItems: (state) => state.list.filter((item) => item.selected),
    selectedQuantity() {
      return this.selectedItems.reduce(
        (sum, item) => sum + (item.quantity || 0),
        0
      );
    },
    selectedAmount() {
      return this.selectedItems.reduce(
        (sum, item) =>
          sum + (item.totalAmount || item.unitPrice * item.quantity || 0),
        0
      );
    },
    isAllSelected(state) {
      return state.list.length > 0 && state.list.every((item) => item.selected);
    },
  },
  actions: {
    setList(list = []) {
      this.list = Array.isArray(list) ? list : [];
      this.loaded = true;
      uni.setStorageSync(config.cartKey, this.list);
    },
    loadFromStorage() {
      if (this.loaded) return;
      const cache = uni.getStorageSync(config.cartKey);
      if (Array.isArray(cache)) {
        this.list = cache;
        this.loaded = true;
      }
    },
    async fetchCart(force = false) {
      if (this.loading) return;
      if (this.loaded && !force) return;
      try {
        this.loading = true;
        const data = await getCartList();
        this.setList(data || []);
      } catch (error) {
        console.error("获取购物车失败", error);
        return Promise.reject(error);
      } finally {
        this.loading = false;
      }
    },
    async addItem(payload) {
      await addToCart(payload);
      await this.fetchCart(true);
    },
    async changeQuantity(id, quantity) {
      await updateCartQuantity(id, quantity);
      this.list = this.list.map((item) =>
        item.id === id ? { ...item, quantity } : item
      );
      uni.setStorageSync(config.cartKey, this.list);
    },
    async toggleSelected(id, selected) {
      await updateCartSelected(id, selected);
      this.list = this.list.map((item) =>
        item.id === id ? { ...item, selected } : item
      );
      uni.setStorageSync(config.cartKey, this.list);
    },
    async toggleSelectAll(selected) {
      const ids = this.list.map((item) => item.id);
      if (ids.length === 0) return;
      await batchUpdateCartSelected(ids, selected);
      this.list = this.list.map((item) => ({ ...item, selected }));
      uni.setStorageSync(config.cartKey, this.list);
    },
    async removeItem(id) {
      await deleteCart(id);
      this.list = this.list.filter((item) => item.id !== id);
      uni.setStorageSync(config.cartKey, this.list);
    },
    async removeItems(ids = []) {
      if (ids.length === 0) return;
      await batchDeleteCart(ids);
      this.list = this.list.filter((item) => !ids.includes(item.id));
      uni.setStorageSync(config.cartKey, this.list);
    },
    async clearCart() {
      await clearCartApi();
      this.setList([]);
    },
  },
});
