package com.upstox.production.idea.controller;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.idea.dto.IdeaFutureMapperRequestDto;
import com.upstox.production.idea.service.IdeaNextFutureMapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fill/idea")
public class IdeaNextFutureMapperController {

    @Autowired
    private IdeaNextFutureMapperService ideaNextFutureMapperService;

    @PostMapping("/nextFutureMapping")
    public String addBankNiftyNextFutureMapper(@RequestBody IdeaFutureMapperRequestDto futureMapperRequestDto) throws UpstoxException {
        return ideaNextFutureMapperService.addNextFutureMapping(futureMapperRequestDto);
    }
}
