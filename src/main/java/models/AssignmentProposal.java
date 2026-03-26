package models;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignmentProposal {
    private Date date;
    private List<GroupProposal> groups = new ArrayList<>();
    private Map<Integer, VehicleSummary> vehicleSummaries = new HashMap<>();

    public AssignmentProposal() {}

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public List<GroupProposal> getGroups() { return groups; }
    public void setGroups(List<GroupProposal> groups) { this.groups = groups; }

    public Map<Integer, VehicleSummary> getVehicleSummaries() { return vehicleSummaries; }
    public void setVehicleSummaries(Map<Integer, VehicleSummary> vehicleSummaries) { this.vehicleSummaries = vehicleSummaries; }

    public static class GroupProposal {
        public List<ReservationProposal> reservations = new ArrayList<>();
        public Time departureTime;
        // Sprint 7: AllocationResult stocké pour persistAllocationResult()
        public transient Object allocationResult; // GroupingService.AllocationResult
    }

    public static class ReservationProposal {
        public int reservationId;
        public Integer proposedVehiculeId; // null if not assigned
        public String reason; // optional explanation
        // Sprint 7: champs pour fragmentation
        public int passengersAssigned = 0;
        public String vehicleAssignments; // ex: "V1:6p, V2:2p"
    }

    public static class VehicleSummary {
        public int vehiculeId;
        public List<Integer> reservationIds = new ArrayList<>();
        public double estimatedKilometrage = 0.0;
        public Timestamp heureDepart;
        public Timestamp heureArrivee;
        // Sprint 7: champs pour fragmentation
        public int totalPassengers = 0;
        public Map<Integer, Integer> passengersPerReservation = new HashMap<>();
    }
}
