package ui.skilltree;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("deprecation")
@Tag("skill-tree")
@JsModule("./src/skill-tree.js")
public class SkillTree extends PolymerTemplate<SkillTreeModel> {

    private List<SkillNode> skillNodes;
    private final List<SkillConnection> skillConnections;

    public SkillTree() {
        skillNodes = new ArrayList<>();
        skillConnections = new LinkedList<>();
        getModel().setSkillNodes(skillNodes);
        updateConnections();
    }

    @ClientCallable
    public void move(String idString, int x, int y) {
        Optional<Integer> optionalId = idFromSkillNodeId(idString);
        if(optionalId.isEmpty() || optionalId.get() == 0) {
            return;
        }

        SkillNode skillNode = skillNodes.get(optionalId.get());
        skillNode.setX(x);
        skillNode.setY(y);
        getModel().setSkillNodes(skillNodes);
        updateConnections();
    }

    @ClientCallable
    public String coordinatesOf(String idString) {
        Optional<Integer> optionalId = idFromSkillNodeId(idString);
        if(optionalId.isEmpty()) {
            return "0:0";
        }

        SkillNode skillNode = skillNodes.get(optionalId.get());
        int x = skillNode.getX();
        int y = skillNode.getY();

        return x + ":" + y;
    }

    @ClientCallable
    public void connectNodes(String idStringFrom, String idStringTo) {
        int from = idFromSkillNodeId(idStringFrom).orElse(-1);
        int to = idFromSkillNodeId(idStringTo).orElse(-1);

        if(from == -1 || to == -1) {
            return;
        }

        connect(from, to);
        updateConnections();
    }

    @EventHandler
    public void handleSkillClick() {
        // unused in skill tree builder
        // clicks are already used to move skill nodes
    }

    public void setSkillNodes(List<SkillNode> skillNodes) {
        getModel().setSkillNodes(skillNodes);
        this.skillNodes = skillNodes;
    }

    public List<SkillNode> getSkillNodes() {
        return skillNodes;
    }

    public void connect(int from, int to) {
        Optional<SkillConnection> connection = skillConnections.stream()
                .filter(skillConnection -> (skillConnection.getFromId() == from && skillConnection.getToId() == to) || (skillConnection.getFromId() == to && skillConnection.getToId() == from))
                .findFirst();
        if (connection.isPresent()) {
            skillConnections.remove(connection.get());
        } else {
            SkillNode fromNode = skillNodes.get(from);
            SkillNode toNode = skillNodes.get(to);
            skillConnections.add(new SkillConnection(fromNode.getId(), toNode.getId(), fromNode.getX(), fromNode.getY(), toNode.getX(), toNode.getY()));
        }
    }

    public void build() {
        updateConnections();
        getModel().setSkillNodes(skillNodes);
    }

    public void updateConnections() {
        skillConnections.forEach(skillConnection -> {
            SkillNode fromNode = skillNodes.get(skillConnection.getFromId());
            SkillNode toNode = skillNodes.get(skillConnection.getToId());

            skillConnection.setX1(fromNode.getX());
            skillConnection.setY1(fromNode.getY());
            skillConnection.setX2(toNode.getX());
            skillConnection.setY2(toNode.getY());
        });
        getModel().setSkillConnections(skillConnections);
    }

    public List<SkillConnection> getSkillConnections() {
        return skillConnections;
    }

    private static Optional<Integer> idFromSkillNodeId(String idString) {
        return Optional.of(idString.split("-"))
                .filter(splitId -> splitId.length == 2)
                .map(splitId -> Integer.parseInt(splitId[1]));
    }
}
