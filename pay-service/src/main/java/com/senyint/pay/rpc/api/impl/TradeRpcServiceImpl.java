package com.senyint.pay.rpc.api.impl;

import com.senyint.common.annotation.RpcService;
import com.senyint.common.util.BeanFactoryUtil;
import com.senyint.pay.constant.TradeStatusEnum;
import com.senyint.pay.constant.TradeTypeEnum;
import com.senyint.pay.dto.OutTradeNoDTO;
import com.senyint.pay.dto.TradeOrderDTO;
import com.senyint.pay.dto.TradeRecordDTO;
import com.senyint.pay.exception.TradeErrorCode;
import com.senyint.pay.exception.TradeException;
import com.senyint.pay.query.TradeQUERY;
import com.senyint.pay.rpc.RpcResult;
import com.senyint.pay.rpc.RpcResultBuilder;
import com.senyint.pay.rpc.api.TradeRpcService;
import com.senyint.pay.service.TradeService;
import com.senyint.pay.vo.TradeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 交易RPC服务实现类
 *
 * @author liuhongming
 */
@RpcService
public class TradeRpcServiceImpl implements TradeRpcService {

    private static final Logger logger = LoggerFactory.getLogger(TradeRpcServiceImpl.class);

    @Autowired
    private TradeService tradeService;

    @Override
    public RpcResult<TradeVO> createTrade(TradeVO tradeVO) throws Exception {
        logger.info("创建交易: {}", tradeVO.toString());

        // 复制交易订单信息
        TradeOrderDTO tradeOrderDTO = BeanFactoryUtil.copyProperties(tradeVO, TradeOrderDTO.class);
        // 交易类型
        tradeOrderDTO.setTradeType(TradeTypeEnum.EXPENSE.name());
        // 交易状态
        tradeOrderDTO.setTradeStatus(TradeStatusEnum.CREATED.name());

        // 复制交易记录信息
        TradeRecordDTO tradeRecordDTO = BeanFactoryUtil.copyProperties(tradeVO, TradeRecordDTO.class);
        // 交易类型
        tradeRecordDTO.setTradeType(TradeTypeEnum.EXPENSE.name());

        RpcResult<TradeVO> rpcResult;
        try {
            OutTradeNoDTO outTradeNoDTO = tradeService.saveTrade(tradeOrderDTO, tradeRecordDTO);
            TradeVO resultVO = new TradeVO();
            resultVO.setOutTradeNo(outTradeNoDTO.toString());
            rpcResult = RpcResultBuilder.instance().success(resultVO);
        } catch (Exception e) {
            rpcResult = RpcResultBuilder.instance().failure("", "创建交易失败：" + e.getMessage());
        }

        return rpcResult;
    }

    @Override
    public RpcResult<TradeVO> queryTrade(TradeQUERY tradeQUERY) throws Exception {
        logger.info("查询交易: {}", tradeQUERY.toString());

        String merchantOrderNo = tradeQUERY.getMerchantOrderNO();
        String tradeNo = tradeQUERY.getTradeNo();

        RpcResult<TradeVO> rpcResult;
        try {
            TradeOrderDTO tradeOrderDTO = tradeService.getTradeOrder(merchantOrderNo, tradeNo);
            if (tradeOrderDTO == null) {
                throw new TradeException(TradeErrorCode.TRADE_NOT_EXIST);
            }
            TradeVO resultVO = BeanFactoryUtil.copyProperties(tradeOrderDTO, TradeVO.class);
            rpcResult = RpcResultBuilder.success(resultVO);
        } catch (TradeException ex) {
            rpcResult = RpcResultBuilder.failure(ex.getErrorCode().getCode(),
                    ex.getErrorCode().getMessage());
        }

        return rpcResult;
    }

    @Override
    public RpcResult<String> updateTrade(TradeVO tradeVO) throws Exception {
        logger.info("更新交易: {}", tradeVO.toString());

        TradeOrderDTO tradeOrderDTO = BeanFactoryUtil.copyProperties(tradeVO, TradeOrderDTO.class);
        TradeRecordDTO tradeRecordDTO = BeanFactoryUtil.copyProperties(tradeVO, TradeRecordDTO.class);

        RpcResult<String> rpcResult;
        try {
            tradeService.updateTradeToComplete(tradeOrderDTO, tradeRecordDTO);
            rpcResult = RpcResultBuilder.instance().success("");
        } catch (Exception e) {
            rpcResult = RpcResultBuilder.instance().failure("", "更新交易：" + e.getMessage());
        }

        return rpcResult;
    }

    @Override
    public RpcResult<String> getResult(String inStr) {
        try {
            String value = inStr.toString() + "<sign>";
            return RpcResultBuilder.instance().success(value);
        } catch (Exception e) {
            return RpcResultBuilder.instance().failure("10001", "空指针异常");
        }
    }

    private boolean checkVO(TradeVO tradeVO) {
        return false;
    }

    private boolean checkQUERY(TradeQUERY tradeQUERY) {
        return false;
    }
}
