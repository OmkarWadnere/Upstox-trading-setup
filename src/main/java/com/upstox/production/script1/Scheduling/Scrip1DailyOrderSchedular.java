package com.upstox.production.script1.Scheduling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.GetPositionDataDto;
import com.upstox.production.centralconfiguration.dto.GetPositionResponseDto;
import com.upstox.production.centralconfiguration.entity.ExceptionalDayMapper;
import com.upstox.production.centralconfiguration.entity.HolidayMapper;
import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.ExceptionalDayMapperRepository;
import com.upstox.production.centralconfiguration.repository.HolidayMapperRepository;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import com.upstox.production.script1.entity.Script1ScheduleOrderMapper;
import com.upstox.production.script1.repository.Script1ScheduleOrderMapperRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class Scrip1DailyOrderSchedular {

    @Autowired
    private Environment environment;

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Autowired
    private Script1ScheduleOrderMapperRepository script1ScheduleOrderMapperRepository;

    @Autowired
    private ExceptionalDayMapperRepository exceptionalDayMapperRepository;
    @Autowired
    private HolidayMapperRepository holidayMapperRepository;

    @Scheduled(cron = "20 15 9 * * ?") // Adjust the cron expression for 9:15:20 AM every morning
    public void Script1DailyTargetOrder() throws UnirestException, IOException, UpstoxException, InterruptedException {

        String token = "Bearer " + getTokenDetails();

        // Validate Non working days
        AtomicBoolean flag = validateNonWorkingDays();
        if (flag.get()) return;

        // Working Days logic
        GetPositionResponseDto getPositionResponseDto = getAllPositionCall(token);

        if (!getPositionResponseDto.getStatus().equalsIgnoreCase("success")) {
            throw new UpstoxException("We are not getting response for get positions from upstox its time to take manual action!!");
        }

        if (getPositionResponseDto.getData().isEmpty()) {
            return;
        }
        Iterable<Script1ScheduleOrderMapper> script1ScheduleOrderMapperRepositoryAll = script1ScheduleOrderMapperRepository.findAll();
        List<Script1ScheduleOrderMapper> script1ScheduleOrderMappers = convertIterableToListSchedularOrderMapper(script1ScheduleOrderMapperRepositoryAll);
        placeScheduleTargetOrder(getPositionResponseDto, script1ScheduleOrderMappers, token);
    }

    private void placeScheduleTargetOrder(GetPositionResponseDto getPositionResponseDto, List<Script1ScheduleOrderMapper> script1ScheduleOrderMappers, String token) throws IOException, InterruptedException {
        for (GetPositionDataDto getPositionDataDto : getPositionResponseDto.getData()) {
            for (Script1ScheduleOrderMapper script1ScheduleOrderMapper : script1ScheduleOrderMappers) {
                if (getPositionDataDto.getInstrumentToken().equalsIgnoreCase(script1ScheduleOrderMapper.getInstrumentToken()) && getPositionDataDto.getQuantity() != 0) {
                    int quantity = getPositionDataDto.getQuantity() < 0 ? getPositionDataDto.getQuantity() * -1 : getPositionDataDto.getQuantity();
                    String requestBody = "{"
                            + "\"quantity\": "+ quantity + ","
                            + "\"product\": \"D\","
                            + "\"validity\": \"DAY\","
                            + "\"price\": "+ script1ScheduleOrderMapper.getTargetPrice() + ","
                            + "\"tag\": \"string\","
                            + "\"instrument_token\": \"" + script1ScheduleOrderMapper.getInstrumentToken() + "\","
                            + "\"order_type\": \"LIMIT\","
                            + "\"transaction_type\": \"" + script1ScheduleOrderMapper.getOrderType() + "\","
                            + "\"disclosed_quantity\": 0,"
                            + "\"trigger_price\": " + script1ScheduleOrderMapper.getTargetPrice() + ","
                            + "\"is_amo\": false"
                            + "}";
                    HttpClient httpClient = HttpClient.newHttpClient();


                    String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(orderUrl))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("Authorization", token)
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();
                    HttpResponse<String> receiveNewOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    // Print the response status code and body
                    log.info("Response Code received from server after placing new order : " + receiveNewOrderResponse.statusCode());
                    log.info("Response Body received from server after placing new order: " + receiveNewOrderResponse.body());
                    script1ScheduleOrderMapperRepository.deleteAll();
                }
            }
        }
    }

    private String getTokenDetails() throws UpstoxException {
        Optional<UpstoxLogin> optionalUpstoxLogin = upstoxLoginRepository.findByEmail(environment.getProperty("email_id"));
        UpstoxLogin upstoxLogin = optionalUpstoxLogin.orElseThrow(() -> new UpstoxException("There is no account for the mail id"));
        return upstoxLogin.getAccess_token();
    }

    private GetPositionResponseDto getAllPositionCall(String token) throws UnirestException, JsonProcessingException {
        log.info("Fetch the current position we are holding");
        Unirest.setTimeouts(0, 0);
        com.mashape.unirest.http.HttpResponse<String> getAllPositionResponse = Unirest.get(environment.getProperty("upstox_url") + environment.getProperty("get_position"))
                .header("Accept", "application/json")
                .header("Authorization", token)
                .asString();

        log.info("Current positions received : " + getAllPositionResponse.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(getAllPositionResponse.getBody(), GetPositionResponseDto.class);
    }

    private AtomicBoolean validateNonWorkingDays() {
        AtomicBoolean flag = new AtomicBoolean(false);
        Iterable<ExceptionalDayMapper> exceptionalDayMappers = exceptionalDayMapperRepository.findAll();
        Iterable<HolidayMapper> holidayMappers = holidayMapperRepository.findAll();
        holidayMappers.forEach((holidayMapper) -> {
            if (LocalDate.now().equals(holidayMapper.getDate())) {
                flag.set(true);
            }
        });
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY) || LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            flag.set(true);
        }
        exceptionalDayMappers.forEach((exceptionalDayMapper) -> {
            if (LocalDate.now().equals(exceptionalDayMapper.getDate())) {
                flag.set(false);
            }
        });
        return flag;
    }

    private static List<Script1ScheduleOrderMapper> convertIterableToListSchedularOrderMapper(Iterable<Script1ScheduleOrderMapper> iterable) {
        List<Script1ScheduleOrderMapper> list = new ArrayList<>();

        for (Script1ScheduleOrderMapper item : iterable) {
            list.add(item);
        }

        return list;
    }
}
