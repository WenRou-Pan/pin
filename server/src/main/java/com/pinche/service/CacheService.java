package com.pinche.service;

import com.pinche.domain.address.Dot;
import com.pinche.domain.address.GeoAddress;
import com.pinche.domain.order.TimeDTO;
import com.pinche.domain.request.FuzzyOrderRequest;
import com.pinche.domain.request.PublishOrderRequest;
import com.pinche.domain.user.UserToken;
import com.pinche.infrastructure.cache.AbstractGeoCache;
import com.pinche.infrastructure.cache.TokenCache;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Parmaze
 * @date 2021/12/16
 */
@Service
public class CacheService {
    @Autowired
    private TokenCache tokenCache;
    @Autowired
    @Qualifier("start")
    private AbstractGeoCache startAbstractGeoCache;
    @Autowired
    @Qualifier("end")
    private AbstractGeoCache endAbstractGeoCache;

    public String addUserId(Integer id, String sessionKey, String openid) {
        String token = RandomStringUtils.randomAlphanumeric(16);
        UserToken userToken = tokenCache.generateToken(token, id, sessionKey, openid);
        return userToken.getToken();
    }

    public Integer getUserId(String token) {
        UserToken userToken = tokenCache.getInfo(token);
        if (userToken == null) {
            return null;
        }
        return userToken.getUserId();
    }

    public String getOpenId(String token) {
        UserToken userToken = tokenCache.getInfo(token);
        if (userToken == null) {
            return null;
        }
        return userToken.getOpenid();
    }

    public List<Integer> findNearOrderId(FuzzyOrderRequest request) {
        List<Integer> startOrderIdlist = startAbstractGeoCache.findOrderId(request.getStartDot());
        List<Integer> endOrderIdlist = endAbstractGeoCache.findOrderId(request.getEndDot());
        startOrderIdlist.retainAll(endOrderIdlist);
        return startOrderIdlist;
    }

    public void publishCache(PublishOrderRequest request, Integer orderId) {
        publishStart(request, orderId);
        publishEnd(request, orderId);
    }

    private void publishStart(PublishOrderRequest request, Integer orderId) {
        GeoAddress address = request.getStartAddress();
        startAbstractGeoCache.add(address.getDot(), orderId, assemble(request.getTimeDTO()));
    }

    private void publishEnd(PublishOrderRequest request, Integer orderId) {
        GeoAddress address = request.getEndAddress();
        endAbstractGeoCache.add(address.getDot(), orderId, assemble(request.getTimeDTO()));
    }

    private LocalDateTime assemble(TimeDTO time) {
        return LocalDateTime.of(time.getYear(), time.getMonth(), time.getDay(), time.getHour(), time.getMinute());
    }

    public List<Integer> startAdvice(Dot dot) {
        return startAbstractGeoCache.findOrderId(dot);
    }
}
