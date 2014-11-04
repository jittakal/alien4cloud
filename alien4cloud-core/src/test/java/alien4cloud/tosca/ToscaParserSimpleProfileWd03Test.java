package alien4cloud.tosca;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import alien4cloud.component.model.IndexedArtifactType;
import alien4cloud.component.model.IndexedCapabilityType;
import alien4cloud.component.model.IndexedNodeType;
import alien4cloud.csar.services.CsarService;
import alien4cloud.tosca.container.model.CSARDependency;
import alien4cloud.tosca.container.model.type.PropertyConstraint;
import alien4cloud.tosca.container.services.csar.ICSARRepositorySearchService;
import alien4cloud.tosca.model.ArchiveRoot;
import alien4cloud.tosca.model.Csar;
import alien4cloud.tosca.model.PropertyDefinition;
import alien4cloud.tosca.parser.ParsingError;
import alien4cloud.tosca.parser.ParsingErrorLevel;
import alien4cloud.tosca.parser.ParsingException;
import alien4cloud.tosca.parser.ParsingResult;
import alien4cloud.tosca.parser.ToscaParser;
import alien4cloud.tosca.properties.constraints.MaxLengthConstraint;
import alien4cloud.tosca.properties.constraints.MinLengthConstraint;
import alien4cloud.utils.MapUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Test tosca parsing for Tosca Simple profile in YAML wd03
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:tosca/parser-application-context.xml")
public class ToscaParserSimpleProfileWd03Test {
    private static final String TOSCA_SPWD03_ROOT_DIRECTORY = "src/test/resources/tosca/SimpleProfile_wd03/parsing/";
    private static final String TOSCA_VERSION = "tosca_simple_yaml_1_0_0_wd03";

    @Resource
    private ToscaParser parser;

    @Resource
    private CsarService csarService;
    @Resource
    private ICSARRepositorySearchService repositorySearchService;

    @Test
    public void testDefinitionVersionValid() throws FileNotFoundException, ParsingException {
        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-definition-version.yml"));
        Assert.assertEquals(0, parsingResult.getContext().getParsingErrors().size());
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
        Assert.assertEquals(TOSCA_VERSION, archiveRoot.getArchive().getToscaDefinitionsVersion());
    }

    @Test(expected = ParsingException.class)
    public void testDefinitionVersionInvalidYaml() throws FileNotFoundException, ParsingException {
        parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-definition-version-invalid.yml"));
    }

    @Test(expected = ParsingException.class)
    public void testDefinitionVersionUnknown() throws FileNotFoundException, ParsingException {
        parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-definition-version-unknown.yml"));
    }

    @Test
    public void testDescriptionSingleLine() throws FileNotFoundException, ParsingException {
        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "description-single-line.yml"));
        Assert.assertEquals(0, parsingResult.getContext().getParsingErrors().size());
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
        Assert.assertEquals(TOSCA_VERSION, archiveRoot.getArchive().getToscaDefinitionsVersion());
        Assert.assertNotNull(archiveRoot.getArchive().getDescription());
        Assert.assertEquals("This is an example of a single line description (no folding).", archiveRoot.getArchive().getDescription());
    }

    @Test
    public void testDescriptionMultiLine() throws FileNotFoundException, ParsingException {
        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "description-multi-line.yml"));
        Assert.assertEquals(0, parsingResult.getContext().getParsingErrors().size());
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
        Assert.assertEquals(TOSCA_VERSION, archiveRoot.getArchive().getToscaDefinitionsVersion());
        Assert.assertNotNull(archiveRoot.getArchive().getDescription());
        Assert.assertEquals(
                "This is an example of a multi-line description using YAML. It permits for line breaks for easier readability...\nif needed.  However, (multiple) line breaks are folded into a single space character when processed into a single string value.\n",
                archiveRoot.getArchive().getDescription());
    }

    @Test
    public void testRootCategories() throws FileNotFoundException, ParsingException {
        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-root-categories.yml"));
        assertNoBlocker(parsingResult);
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
        Assert.assertEquals(TOSCA_VERSION, archiveRoot.getArchive().getToscaDefinitionsVersion());
        Assert.assertEquals("Tosca default namespace value", archiveRoot.getArchive().getToscaDefaultNamespace());
        Assert.assertEquals("Template name value", archiveRoot.getArchive().getName());
        Assert.assertEquals("Temlate author value", archiveRoot.getArchive().getTemplateAuthor());
        Assert.assertEquals("1.0.0-SNAPSHOT", archiveRoot.getArchive().getVersion());
        Assert.assertEquals("This is an example of a single line description (no folding).", archiveRoot.getArchive().getDescription());
    }

    @Ignore
    @Test
    public void testMissingNameFails() throws FileNotFoundException, ParsingException {
        // ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, ""));
        // Assert.assertEquals(0, parsingResult.getContext().getParsingErrors().size());
        // ArchiveRoot archiveRoot = parsingResult.getResult();
        // Assert.assertNotNull(archiveRoot.getArchive());
    }

    @Ignore
    @Test
    public void testImportRelative() throws FileNotFoundException, ParsingException {
        // ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-import-relative.yml"));
        // Assert.assertEquals(0, parsingResult.getContext().getParsingErrors().size());
        // ArchiveRoot archiveRoot = parsingResult.getResult();
        // Assert.assertNotNull(archiveRoot.getArchive());
    }

    @Ignore
    @Test
    public void testImportRelativeMissing() throws FileNotFoundException, ParsingException {
        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-import-relative-missing.yml"));
        Assert.assertEquals(0, parsingResult.getContext().getParsingErrors().size());
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
    }

    @Test
    public void testImportDependency() throws FileNotFoundException, ParsingException {
        Mockito.reset(repositorySearchService);
        Mockito.reset(csarService);
        Csar csar = new Csar("tosca-normative-types", "1.0.0-SNAPSHOT-wd03");
        Mockito.when(csarService.getIfExists(csar.getName(), csar.getVersion())).thenReturn(csar);

        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-import-dependency.yml"));

        Mockito.verify(csarService).getIfExists(csar.getName(), csar.getVersion());

        assertNoBlocker(parsingResult);
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
        Assert.assertNotNull(archiveRoot.getArchive().getDependencies());
        Assert.assertEquals(1, archiveRoot.getArchive().getDependencies().size());
        Assert.assertEquals(new CSARDependency(csar.getName(), csar.getVersion()), archiveRoot.getArchive().getDependencies().iterator().next());
    }

    @Test
    public void testImportDependencyMissing() throws FileNotFoundException, ParsingException {
        Csar csar = new Csar("tosca-normative-types", "1.0.0-SNAPSHOT-wd03");
        Mockito.reset(csarService);
        Mockito.when(csarService.getIfExists(csar.getName(), csar.getVersion())).thenReturn(null);

        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-import-dependency.yml"));

        Mockito.verify(csarService).getIfExists(csar.getName(), csar.getVersion());

        assertNoBlocker(parsingResult);
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
        Assert.assertNotNull(archiveRoot.getArchive().getDependencies());
        Assert.assertEquals(0, archiveRoot.getArchive().getDependencies().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testArtifactType() throws FileNotFoundException, ParsingException {
        Mockito.reset(repositorySearchService);
        IndexedArtifactType mockedResult = Mockito.mock(IndexedArtifactType.class);
        Mockito.when(
                repositorySearchService.getElementInDependencies(Mockito.eq(IndexedArtifactType.class), Mockito.eq("tosca.artifact.Root"),
                        Mockito.any(List.class))).thenReturn(mockedResult);
        Set<String> derivedFromSet = Sets.newHashSet();
        Mockito.when(mockedResult.getDerivedFrom()).thenReturn(derivedFromSet);

        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-artifact-type.yml"));

        assertNoBlocker(parsingResult);
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
        Assert.assertEquals(TOSCA_VERSION, archiveRoot.getArchive().getToscaDefinitionsVersion());
        Assert.assertEquals(1, archiveRoot.getArtifactTypes().size());
        Entry<String, IndexedArtifactType> entry = archiveRoot.getArtifactTypes().entrySet().iterator().next();
        Assert.assertEquals("my_artifact_type", entry.getKey());
        IndexedArtifactType artifact = entry.getValue();
        Assert.assertEquals(Sets.newHashSet("tosca.artifact.Root"), artifact.getDerivedFrom());
        Assert.assertEquals("Java Archive artifact type", artifact.getDescription());
        Assert.assertEquals("application/java-archive", artifact.getMimeType());
        Assert.assertEquals(Lists.newArrayList("jar"), artifact.getFileExt());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCapabilityType() throws FileNotFoundException, ParsingException {
        Mockito.reset(repositorySearchService);
        IndexedCapabilityType mockedResult = Mockito.mock(IndexedCapabilityType.class);
        Mockito.when(
                repositorySearchService.getElementInDependencies(Mockito.eq(IndexedCapabilityType.class), Mockito.eq("tosca.capabilities.Feature"),
                        Mockito.any(List.class))).thenReturn(mockedResult);
        Mockito.when(mockedResult.getDerivedFrom()).thenReturn(Sets.newHashSet("tosca.capabilities.Root"));

        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-capability-type.yml"));
        assertNoBlocker(parsingResult);
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
        Assert.assertEquals(TOSCA_VERSION, archiveRoot.getArchive().getToscaDefinitionsVersion());
        Assert.assertEquals(1, archiveRoot.getCapabilityTypes().size());
        Entry<String, IndexedCapabilityType> entry = archiveRoot.getCapabilityTypes().entrySet().iterator().next();
        Assert.assertEquals("mycompany.mytypes.myapplication.MyFeature", entry.getKey());
        IndexedCapabilityType capability = entry.getValue();
        Assert.assertEquals(Sets.newHashSet("tosca.capabilities.Feature", "tosca.capabilities.Root"), capability.getDerivedFrom());
        Assert.assertEquals("a custom feature of my company’s application", capability.getDescription());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNodeType() throws FileNotFoundException, ParsingException {
        Mockito.reset(repositorySearchService);
        IndexedNodeType mockedResult = Mockito.mock(IndexedNodeType.class);
        Mockito.when(
                repositorySearchService.getElementInDependencies(Mockito.eq(IndexedNodeType.class), Mockito.eq("tosca.nodes.SoftwareComponent"),
                        Mockito.any(List.class))).thenReturn(mockedResult);
        Mockito.when(mockedResult.getDerivedFrom()).thenReturn(Sets.newHashSet("tosca.nodes.Root"));

        Mockito.when(
                repositorySearchService.getElementInDependencies(Mockito.eq(IndexedNodeType.class), Mockito.eq("tosca.nodes.Compute"), Mockito.any(List.class)))
                .thenReturn(mockedResult);
        IndexedCapabilityType mockedCapabilityResult = Mockito.mock(IndexedCapabilityType.class);
        Mockito.when(
                repositorySearchService.getElementInDependencies(Mockito.eq(IndexedCapabilityType.class),
                        Mockito.eq("mytypes.mycapabilities.MyCapabilityTypeName"), Mockito.any(List.class))).thenReturn(mockedCapabilityResult);
        Mockito.when(
                repositorySearchService.getElementInDependencies(Mockito.eq(IndexedCapabilityType.class),
                        Mockito.eq("mytypes.mycapabilities.MyCapabilityTypeName"), Mockito.any(List.class))).thenReturn(mockedCapabilityResult);

        Mockito.when(
                repositorySearchService.getElementInDependencies(Mockito.eq(IndexedCapabilityType.class), Mockito.eq("tosca.capabilities.Endpoint"),
                        Mockito.any(List.class))).thenReturn(mockedCapabilityResult);

        ParsingResult<ArchiveRoot> parsingResult = parser.parseFile(Paths.get(TOSCA_SPWD03_ROOT_DIRECTORY, "tosca-node-type.yml"));

        assertNoBlocker(parsingResult);
        ArchiveRoot archiveRoot = parsingResult.getResult();
        Assert.assertNotNull(archiveRoot.getArchive());
        Assert.assertEquals(TOSCA_VERSION, archiveRoot.getArchive().getToscaDefinitionsVersion());
        Assert.assertEquals(1, archiveRoot.getNodeTypes().size());
        // check node type.
        Entry<String, IndexedNodeType> entry = archiveRoot.getNodeTypes().entrySet().iterator().next();

        Assert.assertEquals("my_company.my_types.MyAppNodeType", entry.getKey());
        IndexedNodeType nodeType = entry.getValue();

        Assert.assertEquals(Sets.newHashSet("tosca.nodes.SoftwareComponent", "tosca.nodes.Root"), nodeType.getDerivedFrom());
        Assert.assertEquals("My company’s custom applicaton", nodeType.getDescription());

        // validate properties parsing
        Assert.assertEquals(2, nodeType.getProperties().size());
        PropertyDefinition def1 = new PropertyDefinition();
        def1.setType("string");
        def1.setDefault("default");
        def1.setDescription("application password");
        List<PropertyConstraint> constraints = Lists.newArrayList();
        constraints.add(new MinLengthConstraint(6));
        constraints.add(new MaxLengthConstraint(10));
        def1.setConstraints(constraints);
        PropertyDefinition def2 = new PropertyDefinition();
        def2.setType("integer");
        def2.setDescription("application port number");
        Assert.assertEquals(MapUtil.newHashMap(new String[] { "my_app_password", "my_app_port" }, new PropertyDefinition[] { def1, def2 }),
                nodeType.getProperties());

        // validate attributes parsing

        // nodeType.getAttributes()
        // nodeType.getInterfaces()
        // nodeType.getCapabilities()
        // nodeType.get
    }

    private void assertNoBlocker(ParsingResult<ArchiveRoot> parsingResult) {
        for (int i = 0; i < parsingResult.getContext().getParsingErrors().size(); i++) {
            ParsingError error = parsingResult.getContext().getParsingErrors().get(i);
            if (error.getErrorLevel().equals(ParsingErrorLevel.ERROR)) {
                System.out.println(error);
            }
            Assert.assertNotEquals(ParsingErrorLevel.ERROR, error.getErrorLevel());
        }
    }
}