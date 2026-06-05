package com.utms.support;

import com.utms.common.security.AuthenticatedUserService;
import com.utms.student.Student;
import com.utms.student.StudentRepository;
import com.utms.support.dto.CreateTicketRequest;
import com.utms.support.dto.TicketResponse;
import com.utms.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

/**
 * UC 1.7 - View Support Information / Send Message to OIDB.
 * SR-3: rate limit — max 1 ticket per 2 minutes per student.
 */
@Service
public class SupportTicketService {

    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(2);

    private final SupportTicketRepository ticketRepository;
    private final StudentRepository studentRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public SupportTicketService(SupportTicketRepository ticketRepository,
                                StudentRepository studentRepository,
                                AuthenticatedUserService authenticatedUserService) {
        this.ticketRepository = ticketRepository;
        this.studentRepository = studentRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        User currentUser = authenticatedUserService.getCurrentUser();
        Student student = studentRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("No student profile found"));

        // UC 1.7 SR-3: rate limiting
        Instant windowStart = Instant.now().minus(RATE_LIMIT_WINDOW);
        long recentCount = ticketRepository.countRecentByStudentId(student.getId(), windowStart);
        if (recentCount >= 1) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "You can only send one message every 2 minutes. Please wait before sending another.");
        }

        SupportTicket ticket = new SupportTicket();
        ticket.setStudent(student);
        ticket.setSubject(request.subject());
        ticket.setCategory(request.category());
        ticket.setMessage(request.message());
        ticket.setTicketStatus("PENDING");
        ticket = ticketRepository.save(ticket);

        return toResponse(ticket);
    }

    private TicketResponse toResponse(SupportTicket t) {
        return new TicketResponse(t.getId(), t.getSubject(), t.getCategory(),
                t.getMessage(), t.getTicketStatus(), t.getCreatedAt());
    }
}
