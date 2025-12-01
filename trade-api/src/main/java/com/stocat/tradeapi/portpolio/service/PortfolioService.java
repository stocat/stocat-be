package com.stocat.tradeapi.portpolio.service;

import com.stocat.tradeapi.portpolio.controller.dto.PortPolioResponse;
import com.stocat.tradeapi.portpolio.controller.dto.PositionResponse;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {

    public PortPolioResponse getPortfolioByUserId (Long userId){
        return new PortPolioResponse();
    }

    public PositionResponse getPositionById (Long positionId, Long userId ) {
        return new PositionResponse();
    }
}
