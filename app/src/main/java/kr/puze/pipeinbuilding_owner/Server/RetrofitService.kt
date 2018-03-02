package kr.puze.pipeinbuilding_owner.Server

import retrofit2.Call
import retrofit2.http.*
import java.util.*

/**
 * Created by parktaejun on 2018. 3. 3..
 */
interface RetrofitService {


    @GET("/")
    fun get(): Call<List<Schema.History>>

    @POST("/")
    @FormUrlEncoded
    fun post(@Field("status") status: Boolean): Call<Schema.History>

    @PUT("/:token")
    @FormUrlEncoded
    fun put(@Query("token") token: String, @Field("status") status: Boolean, @Field("pipeEnergy") pipeEnergy: Number, @Field("energy") energy: Number): Call<Schema.History>
}
