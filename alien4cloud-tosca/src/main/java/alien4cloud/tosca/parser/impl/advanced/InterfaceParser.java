package alien4cloud.tosca.parser.impl.advanced;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;

import com.google.common.collect.Maps;

import alien4cloud.model.components.Interface;
import alien4cloud.model.components.Operation;
import alien4cloud.paas.plan.ToscaNodeLifecycleConstants;
import alien4cloud.paas.plan.ToscaRelationshipLifecycleConstants;
import alien4cloud.tosca.parser.DefferedParsingValueExecutor;
import alien4cloud.tosca.parser.MappingTarget;
import alien4cloud.tosca.parser.ParserUtils;
import alien4cloud.tosca.parser.ParsingContextExecution;
import alien4cloud.tosca.parser.impl.base.ReferencedParser;
import alien4cloud.tosca.parser.mapping.DefaultParser;

@Component
public class InterfaceParser extends DefaultParser<Interface> {
    private static final String INPUTS_KEY = "inputs";
    private static final String TYPE_KEY = "type";
    private static final String DESCRIPTION_KEY = "description";

    @Resource
    private ImplementationArtifactParser implementationArtifactParser;
    private ReferencedParser<Operation> operationParser = new ReferencedParser<>("operation_definition");

    @Override
    public Interface parse(Node node, ParsingContextExecution context) {
        if (node instanceof MappingNode) {
            return parseInterfaceDefinition((MappingNode) node, context);
        } else {
            ParserUtils.addTypeError(node, context.getParsingErrors(), "Interface definition");
        }
        return null;
    }

    private Interface parseInterfaceDefinition(MappingNode node, ParsingContextExecution context) {
        Interface interfaz = new Interface();
        Map<String, Operation> operations = Maps.newHashMap();
        interfaz.setOperations(operations);

        for (NodeTuple entry : node.getValue()) {
            String key = ParserUtils.getScalar(entry.getKeyNode(), context);
            if (INPUTS_KEY.equals(key)) {
                // FIXME process inputs.
            } else if (DESCRIPTION_KEY.equals(key)) {
                interfaz.setDescription(ParserUtils.getScalar(entry.getValueNode(), context));
            } else if (TYPE_KEY.equals(key)) {
                interfaz.setType(getInterfaceType(ParserUtils.getScalar(entry.getValueNode(), context)));
            } else {
                if (entry.getValueNode() instanceof ScalarNode) {
                    Operation operation = new Operation();
                    // implementation artifact parsing should be done using a deferred parser as we need to look for artifact types.
                    BeanWrapper targetBean = new BeanWrapperImpl(operation);
                    MappingTarget target = new MappingTarget("implementationArtifact", implementationArtifactParser);
                    context.addDeferredParser(new DefferedParsingValueExecutor(key, targetBean, context, target, entry.getValueNode()));
                    operations.put(key, operation);
                } else {
                    operations.put(key, operationParser.parse(entry.getValueNode(), context));
                }
            }
        }
        return interfaz;
    }

    /**
     * FIXME PUT THAT SOMEWHERE MORE GLOBAL
     * 
     * @param interfaceType
     * @return
     */
    public static String getInterfaceType(String interfaceType) {
        if (ToscaNodeLifecycleConstants.STANDARD_SHORT.equalsIgnoreCase(interfaceType)) {
            return ToscaNodeLifecycleConstants.STANDARD;
        } else if (ToscaRelationshipLifecycleConstants.CONFIGURE_SHORT.equalsIgnoreCase(interfaceType)) {
            return ToscaRelationshipLifecycleConstants.CONFIGURE;
        }
        return interfaceType;
    }

}