package com.puntomartinez.millete.shared.infrastructure.config.filter;
import com.puntomartinez.millete.shared.domain.ports.out.TimeProvider;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class IndependentProxyBoundaryTest {
 @Test void malformedUntrustedPrefixCannotBuyAnotherLoginBucket() throws Exception {
  TimeProvider clock=mock(TimeProvider.class); when(clock.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
  LoginRateLimitFilter filter;
  try {filter=LoginRateLimitFilter.class.getConstructor(String.class,TimeProvider.class).newInstance("10.0.0.0/8",clock);}
  catch(NoSuchMethodException baseline){filter=LoginRateLimitFilter.class.getConstructor().newInstance();}
  for(int i=0;i<20;i++) assertEquals(200, send(filter,"198.51.100.9,10.0.0.2"));
  int status=send(filter,"garbage,198.51.100.9,10.0.0.2");
  System.out.println("INDEPENDENT: exhausted client plus malformed prefix status="+status);
  assertEquals(429,status,"Untrusted prefix must not move identified client into proxy bucket");
 }
 private int send(LoginRateLimitFilter filter,String xff) throws Exception {
  var req=new MockHttpServletRequest("POST","/api/v1/auth/login");req.setRemoteAddr("10.0.0.1");req.addHeader("X-Forwarded-For",xff);
  var res=new MockHttpServletResponse();filter.doFilter(req,res,(r,s)->{});return res.getStatus();
 }
}
