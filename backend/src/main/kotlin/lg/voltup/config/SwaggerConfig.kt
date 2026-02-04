package lg.voltup.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("포인트 룰렛 API")
                    .description("매일 룰렛을 돌려 포인트를 획득하고 상품을 구매하는 서비스")
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("Point Roulette Team")
                    )
            )
    }
}
