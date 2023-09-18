package pnu.cse.onionmarket.service

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    @GET("/open-api/rest/tracking/state")
    fun deliveryCheck(
        @Query("t_code") waybillCompanyCode: String,
        @Query("t_invoice") waybillNumber: String
    ): Call<String>

    @GET("/open-api/gana/balance")
    fun getWalletMoney(
        @Query("address") walletPrivateKey: String,
    ): Call<String>
}