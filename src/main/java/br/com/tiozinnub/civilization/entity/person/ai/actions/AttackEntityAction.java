package br.com.tiozinnub.civilization.entity.person.ai.actions;

import br.com.tiozinnub.civilization.entity.person.ai.Action;

public class AttackEntityAction extends Action {

//    @Override
//    public void buildParameters(ActionParameterDefinitionBuilder parameters) {
//        parameters.add("target", Entity.class);
//        parameters.add("maxFollowDistance", Double.class).defaultValue(-1d);
//    }

//    @Override
//    public void buildFlow(ActionFlowBuilder flow, ActionParameterReference params) {
////        flow.testIf(f -> f.getPerson().isInAttackRange(params.get("target").asEntity()))
////                .ifTrue(f -> f.getPerson().attack(params.get("target").asEntity()))
////                .ifFalse(f -> f.testIf(params.get("target").asEntity())
////                        .isInRange(params.get("maxFollowDistance").asDouble())
////                        .ifTrue(f1 -> f1.getPerson().moveTo(params.get("target").asEntity(), f1.getPerson().getAttackRange()))
////                        .ifFalse(f1 -> f1.cancel())
////                );
//    }

    @Override
    public String getFlowCode() {

    }

}
