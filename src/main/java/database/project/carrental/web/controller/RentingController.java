package database.project.carrental.web.controller;

import database.project.carrental.model.*;
import database.project.carrental.model.exceptions.ClientNotFoundException;
import database.project.carrental.repository.ClientRepository;
import database.project.carrental.repository.LocationRepository;
import database.project.carrental.repository.VehicleRepository;
import database.project.carrental.service.RentingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping()
public class RentingController {
    private final RentingService rentingService;
    private final LocationRepository locationRepository;
    private final VehicleRepository vehicleRepository;
    private final ClientRepository clientRepository;

    public RentingController(RentingService rentingService, LocationRepository locationRepository, VehicleRepository vehicleRepository, ClientRepository clientRepository) {
        this.rentingService = rentingService;
        this.locationRepository = locationRepository;
        this.vehicleRepository = vehicleRepository;
        this.clientRepository = clientRepository;
    }

    @PostMapping("rent")
    private String addRenting(@RequestParam String licensePlate,
                              @RequestParam Location pickedFrom,
                              @RequestParam(required = false) Location returnedTo,
                              @RequestParam LocalDate startRent,
                              @RequestParam(required = false) LocalDate endRent,
                              HttpServletRequest req,
                              RedirectAttributes redirectAttributes){
        double totalAmount;
        String username=req.getRemoteUser();
        Vehicle vehicle=vehicleRepository.findByLicensePlate(licensePlate);
        if(endRent!=null){
            totalAmount=getTotalAmount(startRent,endRent,vehicle.getDailyPrice());
        }
        else{
            totalAmount=0;
        }
        Client client=this.clientRepository.findByUsername(username).orElseThrow(()->new ClientNotFoundException(username));
        Renting newRenting =  this.rentingService.addRenting(startRent,endRent,totalAmount,vehicle,client,pickedFrom, returnedTo);
        redirectAttributes.addFlashAttribute("rentingId", newRenting.getId());
        return "redirect:/payment";
}
    @GetMapping("/rent/{id}")
    private String showVehicleById(@PathVariable String id, Model model){
        Vehicle vehicle = vehicleRepository.findByLicensePlate(id);
        String typeDescription = vehicle.getVehicleType().getDescription();
        List<Location> allLocations=locationRepository.findAll();

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("vehicleType", typeDescription);
        model.addAttribute("locations",allLocations);
        //Treba da promenam
        return "rent";

    }

private double getTotalAmount(LocalDate startRent, LocalDate endRent, Double dailyPrice) {
    long numberOfDays = ChronoUnit.DAYS.between(startRent, endRent);
    return numberOfDays * dailyPrice;
    }
}