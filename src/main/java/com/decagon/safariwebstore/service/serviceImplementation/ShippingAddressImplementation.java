package com.decagon.safariwebstore.service.serviceImplementation;

import com.decagon.safariwebstore.exceptions.ResourceNotFoundException;
import com.decagon.safariwebstore.model.ShippingAddress;
import com.decagon.safariwebstore.model.User;
import com.decagon.safariwebstore.repository.ShippingAddressRepository;
import com.decagon.safariwebstore.service.ShippingAddressService;
import com.decagon.safariwebstore.utils.MethodUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor

public class ShippingAddressImplementation implements ShippingAddressService {

    private final ShippingAddressRepository shippingAddressRepository;


    @Override
    public List<ShippingAddress> getAllUserShippingAddresses(User user) {
        return shippingAddressRepository
                .findAllByUser(user)
                .orElseThrow(
                        () -> {
                            throw new ResourceNotFoundException("Addresses not found for user!");
                        }
                );
    }

    @Override
    public ShippingAddress getUserDefaultShippingAddress(User user) {
        return getAllUserShippingAddresses(user)
                .stream()
                .filter(shippingAddress -> shippingAddress.getIsDefaultShippingAddress())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Default address not found!"));
    }

    @Override
    public ShippingAddress saveShippingAddress(ShippingAddress shippingAddressRequest, User user) {

        List<ShippingAddress> listOfShippingAddresses = getAllUserShippingAddresses(user);

        if (listOfShippingAddresses.isEmpty()) {
            shippingAddressRequest.setIsDefaultShippingAddress(true);
        } else if (shippingAddressRequest.getIsDefaultShippingAddress()){
            ShippingAddress currentDefaultShippingAddress = getUserDefaultShippingAddress(user);
            currentDefaultShippingAddress.setIsDefaultShippingAddress(false);
            shippingAddressRepository.save(currentDefaultShippingAddress);
        }

        shippingAddressRequest.setUser(user);
        return shippingAddressRepository.save(shippingAddressRequest);
    }

    @Override
    public ShippingAddress editShippingAddress(Long addressId, ShippingAddress shippingAddressRequest, User user) {
        Optional<ShippingAddress> shippingAddress = shippingAddressRepository.findById(addressId);
        if(shippingAddress.isPresent()) {
            if (shippingAddressRequest.getIsDefaultShippingAddress()){
                ShippingAddress currentDefaultShippingAddress = getUserDefaultShippingAddress(user);
                currentDefaultShippingAddress.setIsDefaultShippingAddress(false);
                shippingAddressRepository.save(currentDefaultShippingAddress);
            }
            shippingAddress.get().setAddress(shippingAddressRequest.getAddress());
            shippingAddress.get().setCity(shippingAddressRequest.getCity());
            shippingAddress.get().setState(shippingAddressRequest.getState());
            shippingAddress.get().setPhone(shippingAddressRequest.getPhone());
            shippingAddress.get().setIsDefaultShippingAddress(shippingAddressRequest.getIsDefaultShippingAddress());
            return shippingAddressRepository.save(shippingAddress.get());
        } else {
            throw new ResourceNotFoundException("Shipping Address not found!");
        }
    }

    @Override
    public ResponseEntity<?> deleteShippingAddress(Long addressId) {
        Optional<ShippingAddress> shippingAddress = shippingAddressRepository.findById(addressId);
        if (shippingAddress.isPresent()) {
            shippingAddressRepository.delete(shippingAddress.get());
            return new ResponseEntity<>("ShippingAddress has been deleted.", HttpStatus.OK);
        } else {
            throw new ResourceNotFoundException("ShippingAddress not found!!");
        }
    }

//    @Override
//    public ShippingAddress getAddressByUserAndAddressAndCityAndState(User user, String address, String city, String state) {
//        return shippingAddressRepository.findAddressByUserAndAddressAndCityAndState(user, address,
//                city, state).orElseThrow(
//                () -> {
//                    throw new ResourceNotFoundException("Address not found");
//                }
//        );
//    }
//
//    @Override
//    public Boolean isAddressExisting(ShippingAddress shippingAddress, User user) {
//        return shippingAddressRepository
//                .existsAddressByUserAndAddressAndCityAndState(user, shippingAddress.getAddress(),
//                        shippingAddress.getCity(), shippingAddress.getState());
//    }
}
