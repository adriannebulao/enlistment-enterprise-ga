package com.adriannebulao.enlistment.controllers;

import com.adriannebulao.enlistment.domain.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.testcontainers.junit.jupiter.*;

import java.time.*;
import java.util.*;

import static com.adriannebulao.enlistment.domain.Days.MTH;
import static com.adriannebulao.enlistment.domain.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest
class SectionsControllerIT  {

    @Test
    void createSection_save_to_db() throws Exception {

    }

}
