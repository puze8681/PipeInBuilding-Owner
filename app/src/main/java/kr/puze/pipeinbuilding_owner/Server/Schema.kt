package kr.puze.pipeinbuilding_owner.Server

import java.util.*

/**
 * Created by parktaejun on 2018. 3. 3..
 */
class Schema {
    data class History(var status : Boolean, var startTime: Date,  var token: String, var endTime: Date, var time: Number, var pipeEnergy: Number, var energy: Number, var dif: Number)
}