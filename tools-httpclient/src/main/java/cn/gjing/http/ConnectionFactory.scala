package cn.gjing.http

import java.io.IOException
import java.net.{HttpURLConnection, URL}
import java.security.SecureRandom

import javax.net.ssl.{HttpsURLConnection, SSLContext, TrustManager}

/**
 * @author Gjing
 **/
class ConnectionFactory(requestUrl: String) {
  /**
   * Connect timeout
   */
  var connectTimeout: Int = _
  /**
   * Read timeout
   */
  var readTimeout: Int = _

  def this(requestUrl: String, connectTimeout: Int, readTimeout: Int) {
    this(requestUrl)
    this.connectTimeout = connectTimeout
    this.readTimeout = readTimeout
  }

  /**
   * Get httpsConnection instance
   *
   * @return HttpsURLConnection
   */
  private[http] def getHttps = try {
    val sslContext = SSLContext.getInstance("SSL")
    val tm: Array[TrustManager] = Array(new HttpsManager)
    sslContext.init(null, tm, new SecureRandom)
    val ssf = sslContext.getSocketFactory
    val url = new URL(this.requestUrl)
    val connection = url.openConnection.asInstanceOf[HttpsURLConnection]
    connection.setSSLSocketFactory(ssf)
    connection.setRequestProperty("Charset", "UTF-8")
    connection.setDoOutput(true)
    connection.setDoInput(true)
    connection.setConnectTimeout(this.connectTimeout)
    connection.setReadTimeout(this.readTimeout)
    connection
  } catch {
    case e: Exception =>
      e.printStackTrace()
      null
  }

  /**
   * Get httpConnection instance
   *
   * @return HttpURLConnection
   */
  private[http] def getHttp = try {
    val url = new URL(this.requestUrl)
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestProperty("Charset", "UTF-8")
    connection.setDoOutput(true)
    connection.setDoInput(true)
    connection.setConnectTimeout(this.connectTimeout)
    connection.setReadTimeout(this.readTimeout)
    connection
  } catch {
    case e: IOException =>
      e.printStackTrace()
      null
  }
}
