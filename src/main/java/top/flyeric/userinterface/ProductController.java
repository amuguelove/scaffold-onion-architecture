package top.flyeric.userinterface;

import static top.flyeric.userinterface.assembler.ProductVoMapper.MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import top.flyeric.application.service.ProductApplicationService;
import top.flyeric.domain.model.product.entity.Product;
import top.flyeric.domain.model.weather.adapter.WeatherAdapter;
import top.flyeric.domain.model.weather.entity.Weather;
import top.flyeric.userinterface.vo.QueryProductRequest;

@Slf4j
@RestController
@RequestMapping()
public class ProductController {

    private final ProductApplicationService productApplicationService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherAdapter weatherAdapter;

    public ProductController(ProductApplicationService productApplicationService,
                             StringRedisTemplate stringRedisTemplate,
                             ObjectMapper objectMapper,
                             WeatherAdapter weatherAdapter) {
        this.productApplicationService = productApplicationService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.weatherAdapter = weatherAdapter;
    }

    @ApiOperation("测试Swagger API文档")
    @GetMapping("/hello")
    public String sayHello() {
        return "hello " + LocalDateTime.now();
    }

    @GetMapping("/test")
    public Map testApm(@SortDefault(
            sort = "updateTime", direction = Sort.Direction.DESC) Pageable pageable,
                       @Valid QueryProductRequest request) throws JsonProcessingException {
        // test db
        Page<Product> pagedProducts = productApplicationService.getPagedProducts(request.toPredicates(),
                pageable);

        // test redis
        stringRedisTemplate.opsForValue().set("products",
                objectMapper.writeValueAsString(pagedProducts.getContent()), 1, TimeUnit.DAYS);

        // test third gateway
        Weather todayWeather = weatherAdapter.getTodayWeather();
        log.info("today weather: {}", todayWeather);

        Map result = new LinkedHashMap();
        result.put("products", pagedProducts.map(MAPPER::toResponse));
        result.put("todayWeather", todayWeather);

        return result;
    }
}
