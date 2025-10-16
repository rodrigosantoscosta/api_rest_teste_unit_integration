package com.example.demo;

import com.example.demo.controller.ApiController;
import com.example.demo.model.Item;
import com.example.demo.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {ApiController.class})
public class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    // --- TESTES PARA GET /api/items ---

    @Test
    public void givenNoItems_whenGetAllItems_thenReturns200AndEmptyList() throws Exception {
        when(itemService.getAllItems()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/items")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

//    @Test
//    public void givenItemsExist_whenGetAllItems_thenReturns200AndItemList() throws Exception {
//        List<Item> items = List.of(
//                new Item(1L, "Item 1", "Desc 1"),
//                new Item(2L, "Item 2", "Desc 2")
//        );
//        when(itemService.getAllItems()).thenReturn(items);
//
//        mockMvc.perform(get("/api/items")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$", hasSize(2)))
//                .andExpect(jsonPath("$.name", is("Item 1"))) // Linha corrigida
//                .andExpect(jsonPath("$.[1]name", is("Item 2")));
//    }
@Test
public void givenItemsExist_whenGetAllItems_thenReturns200AndItemList() throws Exception {
    // Cenário (Given)
    List<Item> items = List.of(
            new Item(1L, "Item 1", "Desc 1"),
            new Item(2L, "Item 2", "Desc 2")
    );
    when(itemService.getAllItems()).thenReturn(items);

    // Ação (When) e Verificação (Then)
    mockMvc.perform(get("/api/items")
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2))) // Verifica se o array tem 2 elementos

            // Verifica as propriedades do PRIMEIRO item (índice 0)
            .andExpect(jsonPath("$.[0]id", is(1)))
            .andExpect(jsonPath("$.[0]name", is("Item 1")))
            .andExpect(jsonPath("$.[0]description", is("Desc 1")))

            // Verifica as propriedades do SEGUNDO item (índice 1)
            .andExpect(jsonPath("$.[1]id", is(2)))
            .andExpect(jsonPath("$.[1]name", is("Item 2")))
            .andExpect(jsonPath("$.[1]description", is("Desc 2")));
}

    // --- TESTES DE VALIDAÇÃO ---

    @Test
    public void testCreateItemWithInvalidData_ShouldReturnBadRequest() throws Exception {
        Item invalidItem = new Item(null, "", "Some description");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Name is mandatory")));
    }

    @Test
    public void testCreateItemWithEmptyJson_ShouldReturnBadRequest() throws Exception {
        String emptyItemJson = "{}";

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", is("Name is mandatory")))
                .andExpect(jsonPath("$.description", is("Description is mandatory")));
    }

    // --- TESTES ANTERIORES ---

    @Test
    public void testGetItemJson() throws Exception {
        Item item = new Item(1L, "Item 1", "Description 1");
        when(itemService.getItemById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":1,\"name\":\"Item 1\",\"description\":\"Description 1\"}"));
    }

    @Test
    public void testCreateItem() throws Exception {
        Item itemToCreate = new Item(null, "New Item", "New Description");
        Item createdItem = new Item(1L, "New Item", "New Description");

        when(itemService.createItem(any(Item.class))).thenReturn(createdItem);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemToCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Item")));
    }

    @Test
    public void testGetItemNotFound() throws Exception {
        when(itemService.getItemById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // Adicione estes dois novos métodos ao arquivo ApiControllerTest.java

    @Test
    public void whenUpdateItem_withValidData_thenReturns200AndUpdatedItem() throws Exception {
        // Cenário (Given)
        Item updatedItem = new Item(1L, "Updated Name", "Updated Description");
        // Preparamos o mock: "Quando o método updateItem for chamado com id 1 e qualquer item..."
        when(itemService.updateItem(any(Long.class), any(Item.class))).thenReturn(Optional.of(updatedItem));

        // Ação (When) e Verificação (Then)
        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")));
    }

    @Test
    public void whenUpdateItem_thatDoesNotExist_thenReturns404NotFound() throws Exception {
        // Cenário (Given)
        Item itemData = new Item(999L, "Non Existent", "Data");
        // Preparamos o mock para simular que o item não foi encontrado
        when(itemService.updateItem(any(Long.class), any(Item.class))).thenReturn(Optional.empty());

        // Ação (When) e Verificação (Then)
        mockMvc.perform(put("/api/items/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemData)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void whenDeleteItem_thatExists_thenReturns204NoContent() throws Exception {
        // Cenário (Given)
        // Preparamos o mock para simular que a remoção foi bem-sucedida
        when(itemService.deleteItem(1L)).thenReturn(true);

        // Ação (When) e Verificação (Then)
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent()); // Verifica pelo status 204
    }
}