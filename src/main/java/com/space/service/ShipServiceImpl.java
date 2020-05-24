package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class ShipServiceImpl implements ShipService {

    private ShipRepository shipRepository;

    public ShipServiceImpl() {
    }

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        super();
        this.shipRepository = shipRepository;
    }

    @Override
    public List<Ship> getShips(String name, String planet, ShipType shipType, Long after, Long before,
                               Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                               Integer maxCrewSize, Double minRating, Double maxRating) {

        final Date afterDate;
        if (after == null)
            afterDate = null;
        else
            afterDate = new Date(after);

        final Date beforeDate;
        if (before == null)
            beforeDate = null;
        else
            beforeDate = new Date(before);

        final List<Ship> list = new ArrayList<>();

        Iterable<Ship> iterable = shipRepository.findAll();

        for (Ship ship : iterable) {
            if (name != null && !ship.getName().contains(name)) continue;
            if (planet != null && !ship.getPlanet().contains(planet)) continue;
            if (shipType != null && ship.getShipType() != shipType) continue;
            if (afterDate != null && ship.getProdDate().before(afterDate)) continue;
            if (beforeDate != null && ship.getProdDate().after(beforeDate)) continue;
            if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) continue;
            if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) continue;
            if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) continue;
            if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) continue;
            if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) continue;
            if (minRating != null && ship.getRating().compareTo(minRating) < 0) continue;
            if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) continue;

            list.add(ship);
        }

        return list;
    }

    @Override
    public Ship saveShip(Ship ship) {
        return shipRepository.save(ship);
    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {
        boolean changeRating = false;

        final String name = newShip.getName();
        if (name != null) {
            if (isStringValid(name)) {
                oldShip.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }

        final String planet = newShip.getPlanet();
        if (planet != null) {
            if (isStringValid(planet)) {
                oldShip.setPlanet(planet);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getShipType() != null) {
            oldShip.setShipType(newShip.getShipType());
        }

        final Date prodDate = newShip.getProdDate();
        if (prodDate != null) {
            if (isProdDateValid(prodDate)) {
                oldShip.setProdDate(prodDate);
                changeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getUsed() != null) {
            oldShip.setUsed(newShip.getUsed());
            changeRating = true;
        }

        final Double speed = newShip.getSpeed();
        if (speed != null) {
            if (isSpeedValid(speed)) {
                oldShip.setSpeed(speed);
                changeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        final Integer crewSize = newShip.getCrewSize();
        if (crewSize != null) {
            if (isCrewSizeValid(crewSize)) {
                oldShip.setCrewSize(crewSize);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (changeRating) {
            final double rating = calculateRating(oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate());
            oldShip.setRating(rating);
        }
        shipRepository.save(oldShip);
        return oldShip;
    }

    @Override
    public void deleteShip(Ship ship) {
        shipRepository.delete(ship);
    }

    @Override
    public Ship getShip(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public List<Ship> sortShips(List<Ship> ships, ShipOrder order) {
        if (order != null) {
            Comparator<Ship> idComparator = new Comparator<Ship>() {

                @Override
                public int compare(Ship ship1, Ship ship2) {
                    return ship1.getId().compareTo(ship2.getId());
                }
            };

            Comparator<Ship> speedComparator = new Comparator<Ship>() {

                @Override
                public int compare(Ship ship1, Ship ship2) {
                    return ship1.getSpeed().compareTo(ship2.getSpeed());
                }
            };

            Comparator<Ship> dateComparator = new Comparator<Ship>() {

                @Override
                public int compare(Ship ship1, Ship ship2) {
                    return ship1.getProdDate().compareTo(ship2.getProdDate());
                }
            };

            Comparator<Ship> ratingComparator = new Comparator<Ship>() {

                @Override
                public int compare(Ship ship1, Ship ship2) {
                    return ship1.getRating().compareTo(ship2.getRating());
                }
            };

            Comparator<Ship> comparator = null;

            switch (order) {
                case ID:
                    comparator = idComparator;
                    break;
                case SPEED:
                    comparator = speedComparator;
                    break;
                case DATE:
                    comparator = dateComparator;
                    break;
                case RATING:
                    comparator = ratingComparator;
                    break;
            }
            Collections.sort(ships,comparator);
        }
        return ships;
    }

    @Override
    public double calculateRating(double speed, boolean isUsed, Date prodDate) {
        final double k;
        final int now = 3019;
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(prodDate);
        final int prodYear = calendar.get(Calendar.YEAR);

        if(isUsed)
            k = 0.5;
        else
            k = 1;

        final double rating = (80 * speed * k) / (now - prodYear + 1);

        return Math.round(rating * 100) / 100D;
    }

    @Override
    public List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        final Integer page;
        final Integer size;

        if(pageNumber == null)
            page = 0;
        else
            page = pageNumber;

        if(pageSize == null)
            size = 3;
        else
            size = pageSize;

        final int from = page * size;
        int to = from + size;

        if (to > ships.size())
            to = ships.size();

        return ships.subList(from, to);
    }

    @Override
    public boolean isShipValid(Ship ship) {
        return (ship != null && isStringValid(ship.getName())
                             && isStringValid(ship.getPlanet())
                             && isProdDateValid(ship.getProdDate())
                             && isSpeedValid(ship.getSpeed())
                             && isCrewSizeValid(ship.getCrewSize()));
    }

    private boolean isCrewSizeValid(Integer crewSize) {
        final int minCrewSize = 1;
        final int maxCrewSize = 9999;
        return (crewSize != null && crewSize.compareTo(minCrewSize) >= 0 && crewSize.compareTo(maxCrewSize) <= 0);
    }

    private boolean isSpeedValid(Double speed) {
        final double minSpeed = 0.01;
        final double maxSpeed = 0.99;
        return (speed != null && speed.compareTo(minSpeed) >= 0 && speed.compareTo(maxSpeed) <= 0);
    }

    private boolean isStringValid(String value) {
        final int maxStringLength = 50;
        return (value != null && !value.isEmpty() && value.length() <= maxStringLength);
    }

    private boolean isProdDateValid(Date prodDate) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2800);
        final Date startProd = calendar.getTime();
        calendar.set(Calendar.YEAR, 3019);
        final Date endProd = calendar.getTime();
        return (prodDate != null && prodDate.after(startProd) && prodDate.before(endProd));
    }
}