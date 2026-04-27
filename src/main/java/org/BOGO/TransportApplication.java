package org.BOGO;

import org.BOGO.domain.transport.Location;
import org.BOGO.domain.transport.Map;
import org.BOGO.domain.transport.Stop;
import org.BOGO.service.PathBuildingService;
import java.util.ArrayList;
import java.util.Arrays;

public class TransportApplication {
    public static void main(String[] args) {


        Map map = new Map();

        ArrayList<Integer> C1 = new ArrayList<>();
        ArrayList<Integer> C2 = new ArrayList<>(Arrays.asList(1));
        ArrayList<Integer> C3 = new ArrayList<>(Arrays.asList(2));
        ArrayList<Integer> C4 = new ArrayList<>(Arrays.asList(1, 3));
        ArrayList<Integer> C5 = new ArrayList<>(Arrays.asList(3, 4));
        ArrayList<Integer> C6 = new ArrayList<>(Arrays.asList(5));
        ArrayList<Integer> C7 = new ArrayList<>(Arrays.asList(5));

        ArrayList<Double> f1 = new ArrayList<>();
        ArrayList<Double> f2 = new ArrayList<>(Arrays.asList(1.1));
        ArrayList<Double> f3 = new ArrayList<>(Arrays.asList(2.2));
        ArrayList<Double> f4 = new ArrayList<>(Arrays.asList(4.4, 3.3));
        ArrayList<Double> f5 = new ArrayList<>(Arrays.asList(6.6, 5.5));
        ArrayList<Double> f6 = new ArrayList<>(Arrays.asList(7.7));
        ArrayList<Double> f7 = new ArrayList<>(Arrays.asList(8.8));

        ArrayList<Integer> R1 = new ArrayList<>();
        ArrayList<Integer> R2 = new ArrayList<>(Arrays.asList(1));
        ArrayList<Integer> R3 = new ArrayList<>(Arrays.asList(2));
        ArrayList<Integer> R4 = new ArrayList<>(Arrays.asList(3, 4));
        ArrayList<Integer> R5 = new ArrayList<>(Arrays.asList(2, 3));
        ArrayList<Integer> R6 = new ArrayList<>(Arrays.asList(3));
        ArrayList<Integer> R7 = new ArrayList<>(Arrays.asList(2));

        map.addStop(1,"1",new Location(1,1), C1, f1, R1);
        map.addStop(2,"2",new Location(2,1), C2, f2, R2);
        map.addStop(3,"3",new Location(3,3), C3, f3, R3);
        map.addStop(4,"4",new Location(4,4), C4, f4, R4);
        map.addStop(5,"5",new Location(5,1), C5, f5, R5);
        map.addStop(6,"6",new Location(6,1), C6, f6, R6);
        map.addStop(7,"7",new Location(7,1), C7, f7, R7);

        map.displayMap();

        PathBuildingService pathBuildingService = new PathBuildingService(map);
        pathBuildingService.displayPath(1,6);


    }
}
