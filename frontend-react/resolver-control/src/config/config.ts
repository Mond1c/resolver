import {EvaluatableTo, evaluateConfig} from "@resolver/src/config/config";
import {ResolverControllerConfig} from "./config.interface";

export const controllerConfig = evaluateConfig(getDefaultConfig())

function getDefaultConfig(): EvaluatableTo<ResolverControllerConfig> {
    return {
        CONTROLLER_URL_WS: "ws://localhost:8080/control",
    };
}

export default controllerConfig