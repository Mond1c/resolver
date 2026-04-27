import styled from "styled-components";
import config from "../config/config";
import {ResolverWidgetC, Widget} from "../widgets";

const AwardsWrap = styled.div`
    overflow: hidden;
    display: flex;
    flex-direction: column;
    gap: ${config.AWARDS_GAP};

    top: 0;
    left: 0;
    position: absolute;
    box-sizing: border-box;
    width: 100%;
    height: 100%;
    padding: ${config.AWARDS_PADDING_TOP} ${config.AWARDS_PADDING_RIGHT} 0 ${config.AWARDS_PADDING_LEFT};

    color: white;
    z-index: 1000;

    background-color: ${config.AWARDS_BACKGROUND_COLOR};
    border-radius: ${config.AWARDS_BORDER_RADIUS};
`;

export const Awards: ResolverWidgetC<Widget.AwardsWidget> = (
    {
        widgetData: {settings},
    }
) => {
    return (
        <AwardsWrap>
            <div>Team {settings.teamId} got awards: {settings.awards.map((award) => award.citation).join(", ")}.</div>
        </AwardsWrap>
    );
};

export default Awards;