package com.ble.library.service

// 下行指令
const val CMD_LOGIN: Byte = 0x00.toByte()                       //登录
const val CMD_GENERAL_CONFIG_UPDATE: Byte = 0x01.toByte()       //通用配置更新
const val CMD_UPDATE_VEHICLE_WHITELIST: Byte = 0x02.toByte()    //更新车辆白名单
const val CMD_START_CHARGE: Byte = 0x03.toByte()                //启动充电指令
const val CMD_QUERY_CHARGE_STATE: Byte = 0x04.toByte()          //查询桩状态指令
const val CMD_END_CHARGE: Byte = 0x05.toByte()                  //结束充电指令
const val CMD_QUERY_CONFIG: Byte = 0x06.toByte()                //查询配置信息
const val CMD_QUERY_VEHICLE_WHITELIST: Byte = 0x07.toByte()     //查询车辆白名单
const val CMD_QUERY_HISTORICAL_ORDERS: Byte = 0x08.toByte()     //查询历史订单
const val CMD_OTA_UPDATE: Byte = 0x09.toByte()                  //OTA更新指令
const val CMD_UPDATE_CHARGING_MAX_POWER: Byte = 0x10.toByte()   //更新充电过程中最大充电功率

// 上行指令
const val CMD_START_CHARGE_SUCCESS_RESULT: Byte = 0xA1.toByte()  //启动充电成功的结果推送
const val CMD_START_CHARGE_FAILURE_RESULT: Byte = 0xA2.toByte()  //启动充电失败的结果推送
const val CMD_CHARGE_START_RESULT: Byte = 0xA3.toByte()          //充电状态信息的结果推送
const val CMD_END_CHARGE_SUCCESS_RESULT: Byte = 0xA4.toByte()    //结束充电成功的结果推送
const val CMD_END_CHARGE_FAILURE_RESULT: Byte = 0xA5.toByte()    //结束充电失败的结果推送
const val CMD_OTA_UPDATE_RESULT: Byte = 0xA6.toByte()            //OTA更新结果的推送

