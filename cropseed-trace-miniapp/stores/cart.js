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
      // 统一处理数据，确保有id字段（兼容cartId和id）和selected字段类型
      this.list = (Array.isArray(list) ? list : []).map((item) => {
        const processed = { ...item };

        // 如果只有cartId，添加id字段以便兼容
        if (item.cartId && !item.id) {
          processed.id = item.cartId;
        }
        // 如果只有id，添加cartId字段以便兼容
        if (item.id && !item.cartId) {
          processed.cartId = item.id;
        }

        // 统一selected字段类型为Boolean
        if (typeof processed.selected === "number") {
          processed.selected = processed.selected === 1;
        } else if (
          processed.selected === undefined ||
          processed.selected === null
        ) {
          processed.selected = false;
        }

        return processed;
      });
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
      this.list = this.list.map((item) => {
        const itemId = item.cartId || item.id;
        return itemId === id ? { ...item, quantity } : item;
      });
      uni.setStorageSync(config.cartKey, this.list);
    },
    async toggleSelected(id, selected) {
      await updateCartSelected(id, selected);
      this.list = this.list.map((item) => {
        const itemId = item.cartId || item.id;
        return itemId === id ? { ...item, selected } : item;
      });
      uni.setStorageSync(config.cartKey, this.list);
    },
    async toggleSelectAll(selected) {
      const ids = this.list
        .map((item) => item.cartId || item.id)
        .filter(Boolean);
      if (ids.length === 0) return;
      await batchUpdateCartSelected(ids, selected);
      this.list = this.list.map((item) => ({ ...item, selected }));
      uni.setStorageSync(config.cartKey, this.list);
    },
    async removeItem(id) {
      await deleteCart(id);
      this.list = this.list.filter((item) => {
        const itemId = item.cartId || item.id;
        return itemId !== id;
      });
      uni.setStorageSync(config.cartKey, this.list);
    },
    async removeItems(ids = []) {
      if (ids.length === 0) return;
      await batchDeleteCart(ids);
      this.list = this.list.filter((item) => {
        const itemId = item.cartId || item.id;
        return !ids.includes(itemId);
      });
      uni.setStorageSync(config.cartKey, this.list);
    },
    async clearCart() {
      await clearCartApi();
      this.setList([]);
    },
  },
});
