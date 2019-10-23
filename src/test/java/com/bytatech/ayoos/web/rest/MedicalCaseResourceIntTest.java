package com.bytatech.ayoos.web.rest;

import com.bytatech.ayoos.PatientServiceApp;

import com.bytatech.ayoos.domain.MedicalCase;
import com.bytatech.ayoos.repository.MedicalCaseRepository;
import com.bytatech.ayoos.repository.search.MedicalCaseSearchRepository;
import com.bytatech.ayoos.service.MedicalCaseService;
import com.bytatech.ayoos.service.dto.MedicalCaseDTO;
import com.bytatech.ayoos.service.mapper.MedicalCaseMapper;
import com.bytatech.ayoos.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;


import static com.bytatech.ayoos.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the MedicalCaseResource REST controller.
 *
 * @see MedicalCaseResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PatientServiceApp.class)
public class MedicalCaseResourceIntTest {

    private static final LocalDate DEFAULT_CONSULTED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CONSULTED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final LocalDate DEFAULT_CREATED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_CREATED_DATE = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_DIAGNOSED = "AAAAAAAAAA";
    private static final String UPDATED_DIAGNOSED = "BBBBBBBBBB";

    private static final String DEFAULT_NOTE = "AAAAAAAAAA";
    private static final String UPDATED_NOTE = "BBBBBBBBBB";

    @Autowired
    private MedicalCaseRepository medicalCaseRepository;

    @Autowired
    private MedicalCaseMapper medicalCaseMapper;

    @Autowired
    private MedicalCaseService medicalCaseService;

    /**
     * This repository is mocked in the com.bytatech.ayoos.repository.search test package.
     *
     * @see com.bytatech.ayoos.repository.search.MedicalCaseSearchRepositoryMockConfiguration
     */
    @Autowired
    private MedicalCaseSearchRepository mockMedicalCaseSearchRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restMedicalCaseMockMvc;

    private MedicalCase medicalCase;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final MedicalCaseResource medicalCaseResource = new MedicalCaseResource(medicalCaseService);
        this.restMedicalCaseMockMvc = MockMvcBuilders.standaloneSetup(medicalCaseResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MedicalCase createEntity(EntityManager em) {
        MedicalCase medicalCase = new MedicalCase()
            .consultedDate(DEFAULT_CONSULTED_DATE)
            .createdDate(DEFAULT_CREATED_DATE)
            .diagnosed(DEFAULT_DIAGNOSED)
            .note(DEFAULT_NOTE);
        return medicalCase;
    }

    @Before
    public void initTest() {
        medicalCase = createEntity(em);
    }

    @Test
    @Transactional
    public void createMedicalCase() throws Exception {
        int databaseSizeBeforeCreate = medicalCaseRepository.findAll().size();

        // Create the MedicalCase
        MedicalCaseDTO medicalCaseDTO = medicalCaseMapper.toDto(medicalCase);
        restMedicalCaseMockMvc.perform(post("/api/medical-cases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(medicalCaseDTO)))
            .andExpect(status().isCreated());

        // Validate the MedicalCase in the database
        List<MedicalCase> medicalCaseList = medicalCaseRepository.findAll();
        assertThat(medicalCaseList).hasSize(databaseSizeBeforeCreate + 1);
        MedicalCase testMedicalCase = medicalCaseList.get(medicalCaseList.size() - 1);
        assertThat(testMedicalCase.getConsultedDate()).isEqualTo(DEFAULT_CONSULTED_DATE);
        assertThat(testMedicalCase.getCreatedDate()).isEqualTo(DEFAULT_CREATED_DATE);
        assertThat(testMedicalCase.getDiagnosed()).isEqualTo(DEFAULT_DIAGNOSED);
        assertThat(testMedicalCase.getNote()).isEqualTo(DEFAULT_NOTE);

        // Validate the MedicalCase in Elasticsearch
        verify(mockMedicalCaseSearchRepository, times(1)).save(testMedicalCase);
    }

    @Test
    @Transactional
    public void createMedicalCaseWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = medicalCaseRepository.findAll().size();

        // Create the MedicalCase with an existing ID
        medicalCase.setId(1L);
        MedicalCaseDTO medicalCaseDTO = medicalCaseMapper.toDto(medicalCase);

        // An entity with an existing ID cannot be created, so this API call must fail
        restMedicalCaseMockMvc.perform(post("/api/medical-cases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(medicalCaseDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MedicalCase in the database
        List<MedicalCase> medicalCaseList = medicalCaseRepository.findAll();
        assertThat(medicalCaseList).hasSize(databaseSizeBeforeCreate);

        // Validate the MedicalCase in Elasticsearch
        verify(mockMedicalCaseSearchRepository, times(0)).save(medicalCase);
    }

    @Test
    @Transactional
    public void getAllMedicalCases() throws Exception {
        // Initialize the database
        medicalCaseRepository.saveAndFlush(medicalCase);

        // Get all the medicalCaseList
        restMedicalCaseMockMvc.perform(get("/api/medical-cases?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicalCase.getId().intValue())))
            .andExpect(jsonPath("$.[*].consultedDate").value(hasItem(DEFAULT_CONSULTED_DATE.toString())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].diagnosed").value(hasItem(DEFAULT_DIAGNOSED.toString())))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE.toString())));
    }
    
    @Test
    @Transactional
    public void getMedicalCase() throws Exception {
        // Initialize the database
        medicalCaseRepository.saveAndFlush(medicalCase);

        // Get the medicalCase
        restMedicalCaseMockMvc.perform(get("/api/medical-cases/{id}", medicalCase.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(medicalCase.getId().intValue()))
            .andExpect(jsonPath("$.consultedDate").value(DEFAULT_CONSULTED_DATE.toString()))
            .andExpect(jsonPath("$.createdDate").value(DEFAULT_CREATED_DATE.toString()))
            .andExpect(jsonPath("$.diagnosed").value(DEFAULT_DIAGNOSED.toString()))
            .andExpect(jsonPath("$.note").value(DEFAULT_NOTE.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingMedicalCase() throws Exception {
        // Get the medicalCase
        restMedicalCaseMockMvc.perform(get("/api/medical-cases/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMedicalCase() throws Exception {
        // Initialize the database
        medicalCaseRepository.saveAndFlush(medicalCase);

        int databaseSizeBeforeUpdate = medicalCaseRepository.findAll().size();

        // Update the medicalCase
        MedicalCase updatedMedicalCase = medicalCaseRepository.findById(medicalCase.getId()).get();
        // Disconnect from session so that the updates on updatedMedicalCase are not directly saved in db
        em.detach(updatedMedicalCase);
        updatedMedicalCase
            .consultedDate(UPDATED_CONSULTED_DATE)
            .createdDate(UPDATED_CREATED_DATE)
            .diagnosed(UPDATED_DIAGNOSED)
            .note(UPDATED_NOTE);
        MedicalCaseDTO medicalCaseDTO = medicalCaseMapper.toDto(updatedMedicalCase);

        restMedicalCaseMockMvc.perform(put("/api/medical-cases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(medicalCaseDTO)))
            .andExpect(status().isOk());

        // Validate the MedicalCase in the database
        List<MedicalCase> medicalCaseList = medicalCaseRepository.findAll();
        assertThat(medicalCaseList).hasSize(databaseSizeBeforeUpdate);
        MedicalCase testMedicalCase = medicalCaseList.get(medicalCaseList.size() - 1);
        assertThat(testMedicalCase.getConsultedDate()).isEqualTo(UPDATED_CONSULTED_DATE);
        assertThat(testMedicalCase.getCreatedDate()).isEqualTo(UPDATED_CREATED_DATE);
        assertThat(testMedicalCase.getDiagnosed()).isEqualTo(UPDATED_DIAGNOSED);
        assertThat(testMedicalCase.getNote()).isEqualTo(UPDATED_NOTE);

        // Validate the MedicalCase in Elasticsearch
        verify(mockMedicalCaseSearchRepository, times(1)).save(testMedicalCase);
    }

    @Test
    @Transactional
    public void updateNonExistingMedicalCase() throws Exception {
        int databaseSizeBeforeUpdate = medicalCaseRepository.findAll().size();

        // Create the MedicalCase
        MedicalCaseDTO medicalCaseDTO = medicalCaseMapper.toDto(medicalCase);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMedicalCaseMockMvc.perform(put("/api/medical-cases")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(medicalCaseDTO)))
            .andExpect(status().isBadRequest());

        // Validate the MedicalCase in the database
        List<MedicalCase> medicalCaseList = medicalCaseRepository.findAll();
        assertThat(medicalCaseList).hasSize(databaseSizeBeforeUpdate);

        // Validate the MedicalCase in Elasticsearch
        verify(mockMedicalCaseSearchRepository, times(0)).save(medicalCase);
    }

    @Test
    @Transactional
    public void deleteMedicalCase() throws Exception {
        // Initialize the database
        medicalCaseRepository.saveAndFlush(medicalCase);

        int databaseSizeBeforeDelete = medicalCaseRepository.findAll().size();

        // Delete the medicalCase
        restMedicalCaseMockMvc.perform(delete("/api/medical-cases/{id}", medicalCase.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<MedicalCase> medicalCaseList = medicalCaseRepository.findAll();
        assertThat(medicalCaseList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the MedicalCase in Elasticsearch
        verify(mockMedicalCaseSearchRepository, times(1)).deleteById(medicalCase.getId());
    }

    @Test
    @Transactional
    public void searchMedicalCase() throws Exception {
        // Initialize the database
        medicalCaseRepository.saveAndFlush(medicalCase);
        when(mockMedicalCaseSearchRepository.search(queryStringQuery("id:" + medicalCase.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(medicalCase), PageRequest.of(0, 1), 1));
        // Search the medicalCase
        restMedicalCaseMockMvc.perform(get("/api/_search/medical-cases?query=id:" + medicalCase.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(medicalCase.getId().intValue())))
            .andExpect(jsonPath("$.[*].consultedDate").value(hasItem(DEFAULT_CONSULTED_DATE.toString())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(DEFAULT_CREATED_DATE.toString())))
            .andExpect(jsonPath("$.[*].diagnosed").value(hasItem(DEFAULT_DIAGNOSED)))
            .andExpect(jsonPath("$.[*].note").value(hasItem(DEFAULT_NOTE)));
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MedicalCase.class);
        MedicalCase medicalCase1 = new MedicalCase();
        medicalCase1.setId(1L);
        MedicalCase medicalCase2 = new MedicalCase();
        medicalCase2.setId(medicalCase1.getId());
        assertThat(medicalCase1).isEqualTo(medicalCase2);
        medicalCase2.setId(2L);
        assertThat(medicalCase1).isNotEqualTo(medicalCase2);
        medicalCase1.setId(null);
        assertThat(medicalCase1).isNotEqualTo(medicalCase2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(MedicalCaseDTO.class);
        MedicalCaseDTO medicalCaseDTO1 = new MedicalCaseDTO();
        medicalCaseDTO1.setId(1L);
        MedicalCaseDTO medicalCaseDTO2 = new MedicalCaseDTO();
        assertThat(medicalCaseDTO1).isNotEqualTo(medicalCaseDTO2);
        medicalCaseDTO2.setId(medicalCaseDTO1.getId());
        assertThat(medicalCaseDTO1).isEqualTo(medicalCaseDTO2);
        medicalCaseDTO2.setId(2L);
        assertThat(medicalCaseDTO1).isNotEqualTo(medicalCaseDTO2);
        medicalCaseDTO1.setId(null);
        assertThat(medicalCaseDTO1).isNotEqualTo(medicalCaseDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(medicalCaseMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(medicalCaseMapper.fromId(null)).isNull();
    }
}