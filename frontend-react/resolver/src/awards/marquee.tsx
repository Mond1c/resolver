import React, {useEffect, useRef, useState} from "react";
import styled from "styled-components";

const MarqueeContainer = styled.div`
    overflow: hidden;
    width: 100%;
    height: 100%;
    position: relative;
`

const MarqueeInner = styled.div<{
    duration: number
}>`
    display: flex;
    flex-direction: column;
    animation: marquee-vertical ${props => props.duration}s linear 1 forwards;
    animation-play-state: running;
    animation-fill-mode: forwards;
    @keyframes marquee-vertical {
        0% {
            transform: translateY(0%);
        }
        100% {
            transform: translateY(-100%);
        }
    }
`

interface VerticalMarqueeProps {
    children: React.ReactNode
    speed: number
    className?: string
    style?: React.CSSProperties
}

export const VerticalMarquee = ({
    children,
    speed,
    className,
    style
}: VerticalMarqueeProps) => {
    const contentRef = useRef<HTMLDivElement>(null)
    const [contentHeight, setContentHeight] = useState(0)

    useEffect(() => {
        if (contentRef.current) {
            const height = contentRef.current.offsetHeight
            setContentHeight(height)
        }
    }, [children])

    return <MarqueeContainer
        className={className}
        style={style}>
        <MarqueeInner
            duration={contentHeight / speed}>
            <div ref={contentRef}>{children}</div>
        </MarqueeInner>
    </MarqueeContainer>
}