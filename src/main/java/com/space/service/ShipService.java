package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.Date;
import java.util.List;

public interface ShipService {

    List<Ship> getShips(String name, String planet, ShipType shipType, Long after, Long before,
                        Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                        Integer maxCrewSize, Double minRating, Double maxRating);

    Ship saveShip(Ship ship);

    Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException;

    void deleteShip(Ship ship);

    Ship getShip(Long id);

    List<Ship> sortShips(List<Ship> ships, ShipOrder order);

    double calculateRating(double speed, boolean isUsed, Date prodDate);

    List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize);

    boolean isShipValid(Ship ship);
}