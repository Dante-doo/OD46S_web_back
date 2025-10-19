package utfpr.OD46S.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utfpr.OD46S.backend.dtos.RouteCollectionPointDTO;
import utfpr.OD46S.backend.dtos.RouteDTO;
import utfpr.OD46S.backend.enums.CollectionType;
import utfpr.OD46S.backend.enums.Priority;
import utfpr.OD46S.backend.enums.WasteType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListarRotas() throws Exception {
        mockMvc.perform(get("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routes").isArray())
                .andExpect(jsonPath("$.data.pagination").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBuscarRotaPorId() throws Exception {
        mockMvc.perform(get("/api/v1/routes/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.route").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCriarRota() throws Exception {
        RouteDTO routeDTO = new RouteDTO();
        routeDTO.setName("Test Route");
        routeDTO.setDescription("Test route description");
        routeDTO.setCollectionType(CollectionType.RESIDENTIAL);
        routeDTO.setPeriodicity("0 7 * * 2,4,6");
        routeDTO.setPriority(Priority.MEDIUM);
        routeDTO.setEstimatedTimeMinutes(90);
        routeDTO.setDistanceKm(new BigDecimal("12.5"));
        routeDTO.setActive(true);

        mockMvc.perform(post("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(routeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.route").exists())
                .andExpect(jsonPath("$.message").value("Route created successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdicionarPonto() throws Exception {
        RouteCollectionPointDTO pointDTO = new RouteCollectionPointDTO();
        pointDTO.setSequenceOrder(4);
        pointDTO.setAddress("Test Address");
        pointDTO.setLatitude(new BigDecimal("-25.4284"));
        pointDTO.setLongitude(new BigDecimal("-49.2733"));
        pointDTO.setWasteType(WasteType.RESIDENTIAL);
        pointDTO.setEstimatedCapacityKg(new BigDecimal("30.0"));
        pointDTO.setActive(true);

        mockMvc.perform(post("/api/v1/routes/1/points")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pointDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.point").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReordenarPontos() throws Exception {
        List<Map<String, Integer>> reorderList = new ArrayList<>();
        
        Map<String, Integer> point1 = new HashMap<>();
        point1.put("id", 1);
        point1.put("sequence_order", 3);
        reorderList.add(point1);
        
        Map<String, Integer> point2 = new HashMap<>();
        point2.put("id", 2);
        point2.put("sequence_order", 1);
        reorderList.add(point2);
        
        Map<String, Integer> point3 = new HashMap<>();
        point3.put("id", 3);
        point3.put("sequence_order", 2);
        reorderList.add(point3);

        mockMvc.perform(put("/api/v1/routes/1/points/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reorderList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.route").exists());
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void testListarRotasComoMotorista() throws Exception {
        mockMvc.perform(get("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testListarRotasSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/routes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}

