package utfpr.OD46S.backend.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utfpr.OD46S.backend.dtos.RotaDTO;
import utfpr.OD46S.backend.entitys.Rota;
import utfpr.OD46S.backend.services.RotaService;

@Tag(name = "Rotas", description = "APIs de gerenciamento de rotas")
@RestController
@RequestMapping("/api/v1/routes")
@CrossOrigin(origins = "*")
public class RotaController {


    private RotaService service;
    /*
        Rota endpoint

     */
    @PostMapping("/")
    public ResponseEntity<Rota> cadastrar(@RequestBody RotaDTO rota) {
        try {
            ResponseEntity<Rota> result = service.save(rota);
            return ResponseEntity.status(200).body(result);
        }  catch (RuntimeException e) {

        }

    };
}
