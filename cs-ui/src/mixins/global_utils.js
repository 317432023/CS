/*
 * 全局工具类<br>
 * @usage: <p>
 *     // 在 main.js 中加入
 *     import { mixins } from './mixins/global_utils'
 *     Vue.mixin(mixins)
 *
 *     // 在具体组件页面中如 test.vue 调用
 *     this.global_uuid()
 *     this.global_getQueryString('id')
 * <p>
 * @author Administrator
 * @since 2023-10-4
 */
import {getQueryString, generateRandomString} from "../utils/tools";

export const mixins = {

    methods: {
        /**
         * 获取url参数方法
         * @param name 参数名 必填
         * @param url 地址 可选，若未填写则使用当前url地址
         * @returns {*}
         */
        global_getQueryString(name, url) {
            return getQueryString(name, url)
        },
        global_generateRandomString(length) {
            return generateRandomString(length)
        }
    }

}
