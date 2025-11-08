<template>
    <view class="edit-page">
        <view class="form-item">
            <text class="label">收货人</text>
            <input class="input" v-model="form.consignee" placeholder="请输入收货人姓名" maxlength="20" />
        </view>
        <view class="form-item">
            <text class="label">联系电话</text>
            <input class="input" v-model="form.phone" type="number" placeholder="请输入手机号码" maxlength="11" />
        </view>
        <view class="form-item">
            <text class="label">省市区</text>
            <picker mode="multiSelector" :range="pickerRange" :value="pickerValue" range-key="name"
                @change="onRegionChange" @columnchange="onColumnChange">
                <view class="picker-value">
                    <text v-if="form.province && form.city && form.district">
                        {{ form.province }}{{ form.city }}{{ form.district }}
                    </text>
                    <text v-else class="placeholder">请选择省市区</text>
                </view>
            </picker>
        </view>
        <view class="form-item">
            <text class="label">详细地址</text>
            <textarea class="textarea" v-model="form.detailAddress" placeholder="街道、门牌号等" maxlength="100" />
        </view>
        <view class="form-item switch-item">
            <text class="label">设为默认地址</text>
            <switch :checked="form.isDefault" @change="onDefaultChange" color="#2b9939" />
        </view>

        <button class="submit" type="primary" :loading="loading" @tap="submit">保存</button>
    </view>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
    addAddress,
    updateAddress,
    getAddressDetail
} from '@/api/address.js'
// 导入省市区 JSON 数据
import pcaJsonData from '../../static/address/pca.json'

const form = reactive({
    id: null, // ID可能是字符串（后端序列化为字符串避免精度丢失）或数字
    consignee: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detailAddress: '',
    isDefault: false
})
const loading = ref(false)

// 省市区数据
const pcaData = ref({})
const provinces = ref([])
const cities = ref([])
const districts = ref([])

// picker 数据
const pickerValue = ref([0, 0, 0])
const pickerRange = computed(() => [provinces.value, cities.value, districts.value])

onMounted(() => {
    loadPcaData()
})

onLoad(async (options) => {
    if (options?.id) {
        // 直接使用字符串ID，避免Number转换导致精度丢失
        // 后端会自动将路径参数中的字符串转换为Long类型
        form.id = options.id
        await loadDetail()
    }
})

// 加载省市区数据
function loadPcaData() {
    try {
        // 直接使用导入的 JSON 数据
        pcaData.value = pcaJsonData || {}

        console.log('加载的省市区数据:', Object.keys(pcaData.value).length, '个省份')

        // 转换省份数据
        provinces.value = Object.keys(pcaData.value).map(name => ({ name }))
        console.log('省份列表:', provinces.value.length, '个省份', provinces.value.slice(0, 3).map(p => p.name))

        // 初始化城市和区县
        if (provinces.value.length > 0) {
            updateCities(0)
            updateDistricts(0, 0)
        } else {
            console.error('省份列表为空，数据可能未正确加载')
            uni.showToast({ title: '省市区数据为空，请检查数据文件', icon: 'none' })
            return
        }

        // 如果有已保存的地址，设置选中值
        if (form.province && form.city && form.district) {
            setPickerValueByAddress()
        }
    } catch (error) {
        console.error('加载省市区数据失败', error)
        uni.showToast({ title: '加载地址数据失败: ' + (error.message || '未知错误'), icon: 'none', duration: 3000 })
    }
}

// 根据已保存的地址设置 picker 选中值
function setPickerValueByAddress() {
    const provinceIndex = provinces.value.findIndex(p => p.name === form.province)
    if (provinceIndex >= 0) {
        const provinceName = provinces.value[provinceIndex].name
        const provinceData = pcaData.value[provinceName]
        if (provinceData) {
            const cityKeys = Object.keys(provinceData)
            cities.value = cityKeys.map(name => ({ name }))
            const cityIndex = cities.value.findIndex(c => c.name === form.city)
            if (cityIndex >= 0) {
                const cityName = cities.value[cityIndex].name
                const cityData = provinceData[cityName]
                if (cityData && Array.isArray(cityData)) {
                    districts.value = cityData.map(name => ({ name }))
                    const districtIndex = districts.value.findIndex(d => d.name === form.district)
                    pickerValue.value = [
                        provinceIndex,
                        cityIndex >= 0 ? cityIndex : 0,
                        districtIndex >= 0 ? districtIndex : 0
                    ]
                }
            }
        }
    }
}

// 更新城市列表
function updateCities(provinceIndex) {
    if (provinceIndex >= 0 && provinceIndex < provinces.value.length) {
        const provinceName = provinces.value[provinceIndex].name
        const provinceData = pcaData.value[provinceName]
        if (provinceData) {
            const cityKeys = Object.keys(provinceData)
            cities.value = cityKeys.map(name => ({ name }))
            // 重置城市和区县选中值
            pickerValue.value[1] = 0
            pickerValue.value[2] = 0
            // 更新区县列表
            updateDistricts(provinceIndex, 0)
        }
    }
}

// 更新区县列表
function updateDistricts(provinceIndex, cityIndex) {
    if (provinceIndex >= 0 && provinceIndex < provinces.value.length &&
        cityIndex >= 0 && cityIndex < cities.value.length) {
        const provinceName = provinces.value[provinceIndex].name
        const cityName = cities.value[cityIndex].name
        const provinceData = pcaData.value[provinceName]
        if (provinceData && provinceData[cityName]) {
            const districtData = provinceData[cityName]
            if (Array.isArray(districtData)) {
                districts.value = districtData.map(name => ({ name }))
                // 重置区县选中值
                pickerValue.value[2] = 0
            }
        }
    }
}

// picker 列变化事件
function onColumnChange(e) {
    const column = e.detail.column
    const value = e.detail.value

    if (column === 0) {
        // 省份变化
        pickerValue.value[0] = value
        updateCities(value)
    } else if (column === 1) {
        // 城市变化
        pickerValue.value[1] = value
        updateDistricts(pickerValue.value[0], value)
    } else if (column === 2) {
        // 区县变化
        pickerValue.value[2] = value
    }
}

// picker 确认选择
function onRegionChange(e) {
    const values = e.detail.value
    pickerValue.value = values

    const provinceIndex = values[0]
    const cityIndex = values[1]
    const districtIndex = values[2]

    if (provinceIndex >= 0 && provinceIndex < provinces.value.length) {
        form.province = provinces.value[provinceIndex].name
    }
    if (cityIndex >= 0 && cityIndex < cities.value.length) {
        form.city = cities.value[cityIndex].name
    }
    if (districtIndex >= 0 && districtIndex < districts.value.length) {
        form.district = districts.value[districtIndex].name
    }
}

async function loadDetail() {
    try {
        const data = await getAddressDetail(form.id)
        // 映射数据，将后端返回的 isDefault (Integer) 转换为 Boolean
        // 确保ID保持为字符串类型，避免精度丢失
        form.id = String(data.id || form.id)
        form.consignee = data.consignee || ''
        form.phone = data.phone || ''
        form.province = data.province || ''
        form.city = data.city || ''
        form.district = data.district || ''
        form.detailAddress = data.detailAddress || ''
        form.isDefault = data.isDefault === 1 || data.isDefault === true
        // 加载完详情后，设置 picker 选中值
        if (pcaData.value && Object.keys(pcaData.value).length > 0) {
            setPickerValueByAddress()
        } else {
            // 如果 pca 数据还没加载完，等待加载完成后再设置
            setTimeout(() => {
                if (form.province && form.city && form.district) {
                    setPickerValueByAddress()
                }
            }, 500)
        }
    } catch (error) {
        console.error('获取地址详情失败', error)
    }
}

function onDefaultChange(e) {
    form.isDefault = e.detail.value
}

function validate() {
    if (!form.consignee || !form.consignee.trim()) {
        uni.showToast({ title: '请输入收货人', icon: 'none' })
        return false
    }
    if (!form.phone || !form.phone.trim()) {
        uni.showToast({ title: '请输入联系电话', icon: 'none' })
        return false
    }
    if (!/^1\d{10}$/.test(form.phone)) {
        uni.showToast({ title: '手机号格式不正确', icon: 'none' })
        return false
    }
    if (!form.province || !form.city || !form.district) {
        uni.showToast({ title: '请选择省市区', icon: 'none' })
        return false
    }
    if (!form.detailAddress || !form.detailAddress.trim()) {
        uni.showToast({ title: '请输入详细地址', icon: 'none' })
        return false
    }
    return true
}

async function submit() {
    if (!validate()) return
    loading.value = true
    try {
        // 构建请求数据，将 isDefault 从 Boolean 转换为 Integer (0/1)
        const payload = {
            consignee: form.consignee.trim(),
            phone: form.phone.trim(),
            province: form.province,
            city: form.city,
            district: form.district,
            detailAddress: form.detailAddress.trim(),
            isDefault: form.isDefault ? 1 : 0
        }
        if (form.id) {
            // ID可能是字符串，直接使用，后端会自动转换
            const id = String(form.id) // 确保是字符串类型
            payload.id = id
            await updateAddress(id, payload)
        } else {
            await addAddress(payload)
        }
        uni.showToast({ title: '保存成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
    } catch (error) {
        console.error('保存地址失败', error)
        uni.showToast({ title: error.message || '保存失败', icon: 'none' })
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
.edit-page {
    min-height: 100vh;
    background: linear-gradient(180deg, #f5fff6 0%, #f7f9fb 40%, #ffffff 100%);
    padding: 32rpx 24rpx;
    box-sizing: border-box;
    padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.form-item {
    margin-bottom: 24rpx;
    background: #ffffff;
    border-radius: 20rpx;
    padding: 28rpx 32rpx;
    display: flex;
    align-items: center;
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
    border: 1rpx solid rgba(83, 191, 104, 0.08);
    box-sizing: border-box;
}

.label {
    width: 160rpx;
    font-size: 28rpx;
    color: #333;
    font-weight: 500;
    flex-shrink: 0;
}

.input {
    flex: 1;
    font-size: 28rpx;
    color: #333;
    padding: 0;
    margin: 0;
    border: none;
    background: transparent;
}

.textarea {
    flex: 1;
    font-size: 28rpx;
    color: #333;
    min-height: 160rpx;
    padding: 0;
    margin: 0;
    border: none;
    background: transparent;
    line-height: 1.6;
}

.picker-value {
    flex: 1;
    font-size: 28rpx;
    color: #333;
    padding: 0;
    margin: 0;
    display: flex;
    align-items: center;
}

.picker-value .placeholder {
    color: #999;
}

.switch-item {
    justify-content: space-between;
}

.submit {
    margin-top: 40rpx;
    border-radius: 48rpx;
    background: linear-gradient(90deg, #2b9939, #53bf68);
    height: 96rpx;
    line-height: 96rpx;
    font-size: 32rpx;
    box-shadow: 0 12rpx 30rpx rgba(83, 191, 104, 0.3);
    border: none;
}
</style>
