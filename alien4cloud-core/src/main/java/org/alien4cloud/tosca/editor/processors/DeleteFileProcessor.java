package org.alien4cloud.tosca.editor.processors;

import static alien4cloud.utils.AlienUtils.safe;

import java.nio.file.Path;
import java.util.Map;

import org.alien4cloud.tosca.editor.EditionContextManager;
import org.alien4cloud.tosca.editor.exception.InvalidPathException;
import org.alien4cloud.tosca.editor.operations.DeleteFileOperation;
import org.springframework.stereotype.Component;

import alien4cloud.model.components.DeploymentArtifact;
import alien4cloud.model.components.IArtifact;
import alien4cloud.model.components.Interface;
import alien4cloud.model.components.Operation;
import alien4cloud.model.topology.NodeTemplate;
import alien4cloud.model.topology.RelationshipTemplate;
import alien4cloud.model.topology.Topology;
import alien4cloud.utils.FileUtil;
import alien4cloud.utils.TreeNode;
import lombok.SneakyThrows;

/**
 * This processor is responsible for deletion of a file within the archive.
 */
@Component
public class DeleteFileProcessor implements IEditorCommitableProcessor<DeleteFileOperation>, IEditorOperationProcessor<DeleteFileOperation> {

    @Override
    public void process(DeleteFileOperation operation) {
        Topology topology = EditionContextManager.get().getTopology();

        if (topology.getYamlFilePath().equals(operation.getPath())) {
            throw new InvalidPathException("Topology yaml file cannot be removed.");
        }
        TreeNode target = FileProcessorHelper.getFileTreeNode(operation.getPath());
        target.getParent().getChildren().remove(target);

        for (NodeTemplate nodeTemplate : safe(topology.getNodeTemplates()).values()) {
            for (DeploymentArtifact artifact : safe(nodeTemplate.getArtifacts()).values()) {
                resetRemovedArtifact(artifact, operation.getPath());
            }
            cleanupInterfaces(nodeTemplate.getInterfaces(), operation.getPath());
            for (RelationshipTemplate relationshipTemplate : safe(nodeTemplate.getRelationships()).values()) {
                cleanupInterfaces(relationshipTemplate.getInterfaces(), operation.getPath());
            }
        }
    }

    private void cleanupInterfaces(Map<String, Interface> interfaces, String removedFilePath) {
        for (Interface interfaz : safe(interfaces).values()) {
            for (Operation operation : safe(interfaz.getOperations()).values()) {
                resetRemovedArtifact(operation.getImplementationArtifact(), removedFilePath);
            }
        }
    }

    private void resetRemovedArtifact(IArtifact artifact, String removedFilePath) {
        if (artifact.getArtifactRepository() == null) {
            // this is an archive file, check if reference is good
            if (removedFilePath.equals(artifact.getArtifactRef())) {
                // FIXME is that correct, should we get the default artifact from the node / interface type here ?
                artifact.setArtifactRef(null);
            }
        }
    }

    @Override
    @SneakyThrows
    public void beforeCommit(DeleteFileOperation operation) {
        // remove the file on the local repository
        Path targetPath = EditionContextManager.get().getLocalGitPath().resolve(operation.getPath());
        FileUtil.delete(targetPath);
    }
}