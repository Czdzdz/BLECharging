package com.ble.library.service

import cn.com.heaton.blelibrary.ble.BleLog
import com.ble.library.service.feature.*
import com.ble.library.transform.BLEDispatcher

interface ServiceFactoryDelegate {
    fun didReceive(json: BLEDiagramJson?)
}

class ServiceFactory private constructor(private var delegate: ServiceFactoryDelegate? = null) {

    var diagramInfo: HashMap<Byte, BLEDiagram> = HashMap()

    companion object {
        private var instance: ServiceFactory? = null
            get() {
                if (field == null) {
                    field = ServiceFactory()
                }
                return field
            }

        fun get(): ServiceFactory {
            return instance!!
        }
    }

    init {
        registerDiagram(UpdateGeneralConfig(CMD_GENERAL_CONFIG_UPDATE))
        registerDiagram(UpdateVehicleWhitelist(CMD_UPDATE_VEHICLE_WHITELIST))
        registerDiagram(StartCharge(CMD_START_CHARGE))
        registerDiagram(QueryChargeState(CMD_QUERY_CHARGE_STATE))
        registerDiagram(StopCharge(CMD_END_CHARGE))
        registerDiagram(QueryConfig(CMD_QUERY_CONFIG))
        registerDiagram(QueryVehicleWhitelist(CMD_QUERY_VEHICLE_WHITELIST))
        registerDiagram(QueryHistoricalOrders(CMD_QUERY_HISTORICAL_ORDERS))
        registerDiagram(StartLogin(CMD_LOGIN))
        registerDiagram(OTAUpdate(CMD_OTA_UPDATE))
        registerDiagram(UpdateMaxPower(CMD_UPDATE_CHARGING_MAX_POWER))

        registerDiagram(StartChargeSuccess(CMD_START_CHARGE_SUCCESS_RESULT))
        registerDiagram(StartChargeFail(CMD_START_CHARGE_FAILURE_RESULT))
        registerDiagram(ChargeStateResultData.ChargeStateResult(CMD_CHARGE_START_RESULT))
        registerDiagram(StartChargeSuccess(CMD_START_CHARGE_SUCCESS_RESULT))
        registerDiagram(StopChargeSuccess(CMD_END_CHARGE_SUCCESS_RESULT))
        registerDiagram(OTAUpdateResult(CMD_OTA_UPDATE_RESULT))
    }

    private fun registerDiagram(diagram: BLEDiagram) {
        diagramInfo[diagram.hexCode] = diagram
    }

    /**
     * 执行下行指令
     */
    fun executeDown(json: BLEDiagramJson?) {
        if (json != null) {
            val hexCode = json.hexCode
            val bleDiagram = getBLEDiagram(hexCode)
            if (bleDiagram != null) {
                val jsonToPayload = bleDiagram.jsonToPayload(json)
                if (jsonToPayload != null) {
                    BLEDispatcher.get().sendWithCode(hexCode, jsonToPayload)
                } else {
                    BleLog.e("executeDown", " failure,jsonToPayload is null")
                }
            } else {
                BleLog.e("executeDown", " failure,jsonToPayload is null")
            }
        } else {
            BleLog.e("executeDown", " failure,getBLEDiagram is null")
        }
    }

    /**
     * 执行上行指令
     */
    fun executeUp(hexCode: Byte, payload: ByteArray) {
        val bleDiagram = getBLEDiagram(hexCode)
        if (bleDiagram == null) {
            BleLog.e("executeUp", " failure,getBLEDiagram is null")
        } else {
            val payloadToJson = bleDiagram.payloadToJson(payload)
            BleLog.e("executeUp", "executeUpWithCode --- $payloadToJson")
            delegate?.didReceive(payloadToJson)
        }
    }

    private fun getBLEDiagram(hexCode: Byte): BLEDiagram? {
        return diagramInfo[hexCode]
    }
}