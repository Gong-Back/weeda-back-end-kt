package gongback.pureureum.application

import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.MessageDto
import gongback.pureureum.application.dto.NaverSendMessageDto
import gongback.pureureum.application.dto.PhoneNumberReq
import gongback.pureureum.application.dto.SmsSendResponse
import gongback.pureureum.application.exception.SmsSendException
import gongback.pureureum.application.properties.NaverSmsProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val ALGORITHM = "HmacSHA256"
private const val CHARSET_NAME = "UTF-8"
private const val SENDING_LIMIT = 50

@Service
class NaverSmsService(
    private val webClient: WebClient,
    private val smsLogService: SmsLogService,
    private val naverSmsProperties: NaverSmsProperties
) : SmsService {

    private val SEND_URI = "/sms/v2/services/${naverSmsProperties.serviceId}/messages"

    override fun sendSmsCertification(phoneNumberReq: PhoneNumberReq): SmsSendResponse {
        if (smsLogService.getTotalSize() > SENDING_LIMIT) {
            throw SmsSendException(errorCode = ErrorCode.SMS_SENDING_OVER_REQUEST)
        }

        val randomNumber = getRandomNumber()
        smsLogService.save(phoneNumberReq.phoneNumber)

        sendMessage(phoneNumberReq.receiver, randomNumber)

        return SmsSendResponse(certificationNumber = randomNumber)
    }

    override fun completeCertification(phoneNumberReq: PhoneNumberReq) {
        smsLogService.completeCertification(phoneNumberReq.phoneNumber)
    }

    private fun sendMessage(receiver: String, randomNumber: String) {
        val currentTimeMillis = System.currentTimeMillis()

        webClient
            .post()
            .uri(naverSmsProperties.domain + SEND_URI)
            .headers {
                it.contentType = MediaType.APPLICATION_JSON
                it.set("x-ncp-apigw-timestamp", currentTimeMillis.toString())
                it.set("x-ncp-iam-access-key", naverSmsProperties.accessKey)
                it.set("x-ncp-apigw-signature-v2", makeSignature(currentTimeMillis))
            }
            .bodyValue(
                NaverSendMessageDto(
                    from = naverSmsProperties.phoneNumber,
                    content = "[$randomNumber] ${naverSmsProperties.smsCertificationContent}",
                    messages = listOf(MessageDto(to = receiver))
                )
            )
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .map { _ -> SmsSendException(errorCode = ErrorCode.SMS_SEND_FAILED) }
            }
            .onStatus({ httpStatusCode -> httpStatusCode.is5xxServerError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .map { _ -> SmsSendException(errorCode = ErrorCode.SMS_SEND_FAILED) }
            }
            .bodyToMono(String::class.java)
            .block()
    }

    private fun getRandomNumber(): String {
        val randomCertificationNumber = StringBuilder()
        var size = naverSmsProperties.size
        while (size-- > 0) {
            randomCertificationNumber.append((0..9).random())
        }
        return randomCertificationNumber.toString()
    }

    private fun makeSignature(
        currentTimeMillis: Long
    ): String? {
        val newLine = "\n"
        val url = SEND_URI
        val message = StringBuilder()
            .append("POST")
            .append(" ")
            .append(url)
            .append(newLine)
            .append(currentTimeMillis)
            .append(newLine)
            .append(naverSmsProperties.accessKey)
            .toString()

        val signingKey = SecretKeySpec(naverSmsProperties.secretKey.toByteArray(charset(CHARSET_NAME)), ALGORITHM)

        val mac = Mac.getInstance(ALGORITHM)
        mac.init(signingKey)

        val rawHmac = mac.doFinal(message.toByteArray(charset(CHARSET_NAME)))

        return Base64.getEncoder().encodeToString(rawHmac)
    }
}