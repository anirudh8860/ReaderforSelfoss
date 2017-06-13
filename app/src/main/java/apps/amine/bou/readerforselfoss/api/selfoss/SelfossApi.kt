package apps.amine.bou.readerforselfoss.api.selfoss

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.DispatchingAuthenticator
import com.burgstaller.okhttp.basic.BasicAuthenticator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import apps.amine.bou.readerforselfoss.utils.Config



// codebeat:disable[ARITY,TOO_MANY_FUNCTIONS]
class SelfossApi(c: Context) {

    private val service: SelfossService
    private val config: Config = Config(c)
    private val userName: String
    private val password: String

    init {

        val authCache = ConcurrentHashMap<String, CachingAuthenticator>()

        val httpUserName = config.httpUserLogin
        val httpPassword = config.httpUserPassword
        val credentials = Credentials(httpUserName, httpPassword)
        userName = config.userLogin
        password = config.userPassword

        val authenticator = DispatchingAuthenticator.Builder()
            .with("digest", DigestAuthenticator(credentials))
            .with("basic", BasicAuthenticator(credentials))
            .build()

        val client =
            OkHttpClient
                .Builder()
                .authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(AuthenticationCacheInterceptor(authCache))
                .build()

        val gson =
            GsonBuilder()
                .registerTypeAdapter(Boolean::class.javaPrimitiveType, BooleanTypeAdapter())
                .setLenient()
                .create()


        val retrofit =
            Retrofit
                .Builder()
                .baseUrl(config.baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        service = retrofit.create(SelfossService::class.java)
    }

    fun login(): Call<SuccessResponse> =
        service.loginToSelfoss(config.userLogin, config.userPassword)

    fun readItems(tag: String?, sourceId: Long?, search: String?): Call<List<Item>> =
        getItems("read", tag, sourceId, search)

    fun newItems(tag: String?, sourceId: Long?, search: String?): Call<List<Item>> =
        getItems("unread", tag, sourceId, search)

    fun starredItems(tag: String?, sourceId: Long?, search: String?): Call<List<Item>> =
        getItems("starred", tag, sourceId, search)

    private fun getItems(type: String, tag: String?, sourceId: Long?, search: String?): Call<List<Item>> =
        service.getItems(type, tag, sourceId, search, userName, password)

    fun markItem(itemId: String): Call<SuccessResponse> =
        service.markAsRead(itemId, userName, password)

    fun unmarkItem(itemId: String): Call<SuccessResponse> =
        service.unmarkAsRead(itemId, userName, password)

    fun readAll(ids: List<String>): Call<SuccessResponse> =
        service.markAllAsRead(ids, userName, password)

    fun starrItem(itemId: String): Call<SuccessResponse> =
        service.starr(itemId, userName, password)

    fun unstarrItem(itemId: String): Call<SuccessResponse> =
        service.unstarr(itemId, userName, password)

    val stats: Call<Stats>
        get() = service.stats(userName, password)

    val tags: Call<List<Tag>>
        get() = service.tags(userName, password)

    fun update(): Call<String> =
        service.update(userName, password)

    val sources: Call<List<Sources>>
        get() = service.sources(userName, password)

    fun deleteSource(id: String): Call<SuccessResponse> =
        service.deleteSource(id, userName, password)

    fun spouts(): Call<Map<String, Spout>> =
        service.spouts(userName, password)

    fun createSource(title: String, url: String, spout: String, tags: String, filter: String): Call<SuccessResponse> =
        service.createSource(title, url, spout, tags, filter, userName, password)

}
// codebeat:enable[ARITY,TOO_MANY_FUNCTIONS]