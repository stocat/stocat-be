package com.stocat.tradeapi.portpolio.controller;

import com.stocat.common.response.ApiResponse;
import com.stocat.tradeapi.portpolio.controller.dto.PortPolioResponse;
import com.stocat.tradeapi.portpolio.controller.dto.PositionResponse;
import com.stocat.tradeapi.portpolio.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/portfolio")
public class ProtfolioController {

    private final PortfolioService portfolioService;

    public ProtfolioController(PortfolioService portfolioService){
        this.portfolioService = portfolioService;
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<PortPolioResponse>> getPortfolioByUserId(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(portfolioService.getPortfolioByUserId(userId)));
    }

    @GetMapping("/{positionId}")
    public ResponseEntity<ApiResponse<PositionResponse>> getPositionById (@PathVariable Long positionId, @RequestParam Long userId){
        return ResponseEntity.ok(ApiResponse.success(portfolioService.getPositionById(positionId, userId)));
    }
}
