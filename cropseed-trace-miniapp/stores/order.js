import { defineStore } from "pinia";
import { createOrder, getOrderList, getOrderDetail } from "@/api/order.js";

export const useOrderStore = defineStore("order", {
  state: () => ({
    confirmItems: [],
    confirmSource: "",
    submitLoading: false,
  }),
  getters: {
    confirmAmount(state) {
      return state.confirmItems.reduce(
        (sum, item) => sum + (item.unitPrice || 0) * (item.quantity || 0),
        0
      );
    },
  },
  actions: {
    setConfirmItems(items = [], source = "") {
      this.confirmItems = Array.isArray(items) ? items : [];
      this.confirmSource = source;
    },
    clearConfirm() {
      this.confirmItems = [];
      this.confirmSource = "";
    },
    async submitOrder(payload) {
      if (this.submitLoading) return null;
      this.submitLoading = true;
      try {
        const data = await createOrder(payload);
        return data;
      } finally {
        this.submitLoading = false;
      }
    },
    async fetchOrders(params) {
      const data = await getOrderList(params);
      return data;
    },
    async fetchOrderDetail(orderId) {
      const data = await getOrderDetail(orderId);
      return data;
    },
  },
});
